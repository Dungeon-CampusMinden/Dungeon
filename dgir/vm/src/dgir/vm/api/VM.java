package dgir.vm.api;

import dgir.core.debug.Location;
import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.core.traits.INoTerminator;
import dgir.vm.dap.DebugUtils;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static dgir.dialect.builtin.BuiltinOps.ProgramOp;

public class VM {
  private @Nullable ProgramOp program;
  private @Nullable State state;
  private @Nullable Action lastAction;

  private final @NotNull Deque<@NotNull Operation> opStack = new ArrayDeque<>();

  // =========================================================================
  // Debug support
  // =========================================================================

  /** Optional debugger callback; {@code null} means debug mode is off. */
  private @Nullable Debugger debugger;

  /** Active breakpoints. Only consulted when a {@link Debugger} is attached. */
  private final @NotNull Set<Breakpoint> breakpoints = new HashSet<>();

  /**
   * The breakpoints that have been hit in the current stackframe. This is used to prevent firing
   * the same breakpoint multiple times when stepping through an op that matches a breakpoint (e.g.
   * because it has multiple source locations. The set is cleared whenever we step or jump to a
   * different operation, so the same breakpoint can be hit again if we return to the same line
   * later.
   */
  private final @NotNull Deque<@NotNull Optional<Breakpoint>> currentHitBreakpoints =
      new ArrayDeque<>();

  /**
   * Pause/resume lock. When a debugger callback returns {@link DebugControl#PAUSE} the VM thread
   * waits on {@link #resumeCondition}; {@link #resume()} or {@link #stepOver()} signals it.
   */
  private final @NotNull ReentrantLock pauseLock = new ReentrantLock();

  private final @NotNull Condition resumeCondition = pauseLock.newCondition();
  private volatile boolean paused = false;

  /** Distinguishes between the three single-step modes used by the debugger. */
  private enum StepMode {
    /** Normal execution — no pending single-step. */
    NONE,
    /**
     * Step-in: pause at the very next operation, regardless of call depth. Equivalent to the DAP
     * "stepIn" command.
     */
    STEP_IN,
    /**
     * Step-over: keep running until the call-stack depth has returned to {@link #stepTargetDepth},
     * then pause. Equivalent to the DAP "next" command.
     */
    STEP_OVER,
    /**
     * Step-out: keep running until the call-stack depth has returned to 0, then pause. Equivalent
     * to the DAP "stepOut" command.
     */
    STEP_OUT
  }

  /** The active single-step mode; only consulted while the VM is running. */
  private volatile StepMode stepMode = StepMode.NONE;

  /**
   * The call-stack depth that must be reached (or gone below) before the VM pauses again when
   * {@link #stepMode} is {@link StepMode#STEP_OVER}.
   */
  private volatile int stepTargetDepth = 0;

  /**
   * The source location (file + line) of the operation that was <em>current</em> when a {@link
   * #stepOver()} or {@link #stepIn()} command was issued.
   *
   * <p>Used to implement <em>line-granular</em> stepping: if the depth condition is satisfied but
   * the next operation still belongs to the same source line, the VM keeps executing silently
   * instead of pausing. This prevents the user from having to press "next" multiple times to
   * advance through a single high-level source statement that lowers to several IR operations.
   *
   * <p>Set to {@link Location#UNKNOWN} when no step is pending.
   */
  private volatile Location stepOriginLocation = Location.UNKNOWN;

  // =========================================================================
  // Construction
  // =========================================================================

  public VM() {}

  // =========================================================================
  // Program / state management
  // =========================================================================

  public @Nullable ProgramOp getProgram() {
    return program;
  }

  public void init(@NotNull ProgramOp program) {
    assert program.verify(true) : "Program is invalid.";
    this.program = program;
    state = new State();
  }

  private void resetState() {
    assert state != null : "VM not initialized with a state.";
    assert program != null : "VM not initialized with a program.";
    state.reset();
    opStack.clear();
    opStack.push(program.getOperation());
    lastAction = null;
    OpRunnerRegistry.clearRunnerStates();
  }

  // =========================================================================
  // Debug API
  // =========================================================================

  /**
   * Attach a {@link Debugger} to this VM. While a debugger is attached the VM calls {@link
   * Debugger#onStep} (and {@link Debugger#onBreakpointHit} when applicable) before each operation
   * is dispatched. Pass {@code null} to detach and disable debug mode.
   *
   * @param debugger the debugger to attach, or {@code null} to disable debug mode.
   */
  public void setDebugger(@Nullable Debugger debugger) {
    this.debugger = debugger;
  }

  /**
   * Returns the currently attached {@link Debugger}, or {@code Optional.empty()} if none is
   * attached.
   *
   * @return the active debugger.
   */
  public @NotNull Optional<Debugger> getDebugger() {
    return Optional.ofNullable(debugger);
  }

  /**
   * Register a {@link Breakpoint}. The VM calls {@link Debugger#onBreakpointHit} whenever an
   * operation whose source location matches this breakpoint is about to be executed.
   *
   * @param breakpoint the breakpoint to add.
   */
  public void addBreakpoint(@NotNull Breakpoint breakpoint) {
    breakpoints.add(breakpoint);
  }

  /**
   * Remove a previously registered {@link Breakpoint}.
   *
   * @param breakpoint the breakpoint to remove.
   */
  public void removeBreakpoint(@NotNull Breakpoint breakpoint) {
    breakpoints.remove(breakpoint);
  }

  /** Remove all registered breakpoints. */
  public void clearBreakpoints() {
    breakpoints.clear();
  }

  /**
   * Returns an unmodifiable view of the currently registered breakpoints.
   *
   * @return the active breakpoints.
   */
  public @NotNull @Unmodifiable Set<Breakpoint> getBreakpoints() {
    return Collections.unmodifiableSet(breakpoints);
  }

  /**
   * Return the {@link Location} of the operation that would be executed next, or {@link
   * Location#UNKNOWN} if the stack is empty or the VM has not been initialised.
   *
   * <p>This may be called from any thread (including while the VM is paused) to obtain the current
   * program counter for DAP {@code StoppedEvent} / {@code StackTraceResponse} payloads.
   *
   * @return the current source location.
   */
  public @NotNull Location getCurrentLocation() {
    if (opStack.isEmpty()) return Location.UNKNOWN;
    return opStack.peek().getLocation();
  }

  /**
   * Returns the current {@link State}, or {@code Optional.empty()} if the VM has not been
   * initialised yet. Callers may inspect the state while the VM is paused to populate DAP {@code
   * VariablesResponse} payloads.
   *
   * @return the active state.
   */
  public @NotNull Optional<State> getState() {
    return Optional.ofNullable(state);
  }

  /**
   * Resume a paused VM. Unblocks the VM thread waiting inside {@link #run()} or {@link #step()}.
   * Calling this when the VM is not paused is a no-op.
   */
  public void resume() {
    pauseLock.lock();
    try {
      paused = false;
      stepMode = StepMode.NONE;
      resumeCondition.signalAll();
    } finally {
      pauseLock.unlock();
    }
  }

  /**
   * Step over the current operation: execute it (including any nested regions or function calls)
   * and pause at the next operation at the same call-stack depth. Implements the DAP "next"
   * command. Calling this when the VM is not paused is a no-op.
   *
   * <p>Stepping is <em>line-granular</em>: if multiple IR operations share the same source line,
   * the VM advances silently past all of them and only pauses when the next operation belongs to a
   * different source line (or the program ends).
   */
  public void stepOver() {
    assert state != null : "VM not initialised with a state.";
    pauseLock.lock();
    try {
      paused = false;
      stepMode = StepMode.STEP_OVER;
      // Record the current callstack depth so we can detect when we have returned to
      // the same "level". The op currently on top will be executed next (and may push
      // nested ops); we want to pause once the stack is back at this depth.
      stepTargetDepth = state.getCallStack().size();
      // Remember which source line we are stepping from so we can skip over peer IR
      // operations that belong to the same line.
      stepOriginLocation = getCurrentLocation();
      resumeCondition.signalAll();
    } finally {
      pauseLock.unlock();
    }
  }

  /**
   * Step into the current operation: pause at the very next operation executed, which may be inside
   * a nested region or function call. Implements the DAP "stepIn" command. Calling this when the VM
   * is not paused is a no-op.
   *
   * <p>Stepping is <em>line-granular</em>: if the next operation still belongs to the same source
   * line, the VM keeps advancing until it reaches a different line (or the program ends).
   */
  public void stepIn() {
    pauseLock.lock();
    try {
      paused = false;
      stepMode = StepMode.STEP_IN;
      // Remember which source line we are stepping from so we can skip over peer IR
      // operations that belong to the same line.
      stepOriginLocation = getCurrentLocation();
      resumeCondition.signalAll();
    } finally {
      pauseLock.unlock();
    }
  }

  /**
   * Step out of the current function: keep running until the call stack is back at the level of the
   * current function's caller, then pause. Implements the DAP "stepOut" command. Calling this when
   * the VM is not paused is a no-op.
   *
   * <p>Stepping is <em>line-granular</em>: if the next operation still belongs to the same source
   * line, the VM keeps advancing until it reaches a different line (or the program ends
   */
  public void stepOut() {
    assert state != null : "VM not initialised with a state.";
    pauseLock.lock();
    try {
      paused = false;
      stepMode = StepMode.STEP_OUT;
      // Record the current callstack depth -1 so we can step through the outer stack frame as
      // expected without entering
      // called methods.
      stepTargetDepth = state.getCallStack().size() - 1;
      stepOriginLocation = getCurrentLocation();
      resumeCondition.signalAll();
    } finally {
      pauseLock.unlock();
    }
  }

  // =========================================================================
  // Execution
  // =========================================================================

  public boolean run() {
    if (program == null) {
      System.err.println("VM not initialized with a program.");
      return false;
    }

    resetState();

    Action currentAction = Action.Next();
    while (!(currentAction instanceof Action.Abort) && !opStack.isEmpty()) {
      currentAction = step();
    }

    if (currentAction instanceof Action.Abort(String message, Optional<Exception> exception)) {
      exception.ifPresent(
          e -> {
            System.err.println("Exception during execution. Exception message: " + e.getMessage());
            e.printStackTrace(System.err);
          });
      return false;
    }

    System.out.println(
        "Program completed successfully. Total instructions executed: " + state.instructionCount);
    return true;
  }

  public @NotNull Action step() {
    try {
      assert !opStack.isEmpty() : "No operation to execute.";
      assert state != null : "No state to execute the operation in.";

      Operation currentOp = opStack.peek();

      // We reached the end of the program. This is a special case since the operation will not
      // push a next operation onto the stack and we would loop endlessly if we did not stop here.
      if (currentOp.hasTrait(INoTerminator.class) && lastAction instanceof Action.Terminate) {
        opStack.pop();
        return Action.Next();
      }

      // ------------------------------------------------------------------
      // Debug hook: notify the debugger before executing the operation.
      // ------------------------------------------------------------------
      if (debugger != null) {
        Location location = currentOp.getLocation();

        // Only hit breakpoints if we handled the entry hit. It has priority and overshadows
        // breakpoints.
        if (debugger.entryHit()) {
          // Check breakpoints first; only fire the first matching one per step.
          // Breakpoints always interrupt execution, even during a step-over.
          for (Breakpoint bp : breakpoints) {
            if (DebugUtils.breakpointMatches(bp, location)
                && (currentHitBreakpoints.isEmpty() || currentHitBreakpoints.peek().isEmpty())) {
              DebugControl bpCtrl = debugger.onBreakpointHit(currentOp, bp, location);
              if (bpCtrl == DebugControl.PAUSE) {
                // Push the breakpoint onto the stack so that we can detect when it is hit again.
                currentHitBreakpoints.pop();
                currentHitBreakpoints.push(Optional.of(bp));
                // A breakpoint hit cancels any pending step mode so the prior step context
                // does not bleed into the next user command.
                stepMode = StepMode.NONE;
                stepOriginLocation = Location.UNKNOWN;
                // Block until the user issues continue/next/stepIn/stepOut.
                waitForResume();
              }
              break;
            }
          }
        } else {
          // Avoid hitting the breakpoint after stopping on entry by pushing marking the breakpoint
          // on the entry line as hit
          for (Breakpoint bp : breakpoints) {
            if (DebugUtils.breakpointMatches(bp, location)) {
              // Push the breakpoint onto the stack so that we can detect when it is hit again.
              currentHitBreakpoints.pop();
              currentHitBreakpoints.push(Optional.of(bp));
              stepOriginLocation = location;
              break;
            }
          }
        }

        // Skip the pre-execution onStep when any step mode is active: the step-complete
        // notification is delivered in the re-arm block below, after the op has executed.
        // This prevents the debugger's stepPending flag from being consumed prematurely
        // on the op that was already paused on before the step command was issued.
        // Breakpoints still fire above regardless of step mode.
        boolean inStepMode = stepMode != StepMode.NONE;

        if (!inStepMode) {
          DebugControl stepCtrl = debugger.onStep(currentOp, location);
          if (stepCtrl == DebugControl.PAUSE) {
            waitForResume();
          }
        }
      }

      Action currentAction = stepImpl();
      switch (currentAction) {
        // Just continue to the next operation in the current block.
        case Action.Next ignored -> {
          currentOp
              .getNext()
              .ifPresentOrElse(
                  opStack::push,
                  () -> {
                    currentOp.emitError("Reached end of block without an explicit jump or return.");
                    cleanupAfterAbort();
                  });
        }
        // Abort the execution.
        case Action.Abort abort -> {
          currentOp.emitError("Execution aborted: " + abort.message());
          cleanupAfterAbort();
        }
        // Call another function. This is only used for function calls.
        case Action.Call call -> handleCall(call, currentOp);
        // Jump to another block in the same region. This is used for control flow operations like
        // if and while.
        case Action.JumpToBlock jumpToBlock -> handleJumpToBlock(jumpToBlock);
        // Jump to another region in the same block. This is used for control flow operations like
        // while which have a
        // separate region for the body and the condition check logic.
        case Action.JumpToRegion jumpToRegion -> handleJumpToRegion(jumpToRegion, currentOp);
        // Return from the current region. This is used for function calls, as well as for returning
        // from if and while
        // blocks and similar structured control flow ops
        case Action.Terminate aTerminate -> handleTerminate(aTerminate);
        // Step into a region. This is used for nested regions like the then and else regions of an
        // if operation, or the
        // body of a while operation, as well as function calls.
        // It opens a new stack frame for the region and jumps to the first operation in the region.
        case Action.StepIntoRegion stepIntoRegion ->
            handleStepIntoRegion(stepIntoRegion, currentOp);
      }
      Operation nextOp = opStack.peek();
      Location nextLocation = nextOp != null ? nextOp.getLocation() : currentOp.getLocation();

      // Line-granularity: even if the depth is right, keep running silently while the
      // next operation is still on the same source line we stepped from.  This prevents
      // the user having to press "next" multiple times to advance through a single
      // high-level statement that lowers to several IR operations on the same line.
      //
      // Special cases that always pause regardless of line:
      //   • The program is about to end (nextOp == null) — we must pause so the debugger
      //     can show the final state before the exited event fires.
      //   • The origin location is UNKNOWN — locations are not tracked for this program,
      //     so fall back to op-granular stepping.
      boolean onSameLine =
          nextOp != null
              && !stepOriginLocation.equals(Location.UNKNOWN)
              && nextLocation.file().equals(stepOriginLocation.file())
              && nextLocation.line() == stepOriginLocation.line();

      // If we are currently on a breakpoint, check if the next operation is still on the same
      // breakpoint. If not, we can remove the breakpoint from the stack and allow it to be hit
      // again if we return to the same line later.
      if (!currentHitBreakpoints.isEmpty() && currentHitBreakpoints.peek().isPresent()) {
        boolean onSameBreakpoint = isOnSameBreakpoint(onSameLine, nextLocation);

        // Remove the current breakpoint since we might hit another one on the same line next
        if (!onSameBreakpoint) {
          currentHitBreakpoints.pop();
          currentHitBreakpoints.push(Optional.empty());
        }
      }

      // If we did a single-step, decide whether to pause now.
      if (stepMode != StepMode.NONE) {
        // Depth condition: have we returned to the level we started stepping at?
        boolean depthConditionMet =
            switch (stepMode) {
              // Step-in: depth is always satisfied after any single operation.
              case STEP_IN -> true;
              // Step-over: only once the call-stack depth is back at (or below) the target.
              case STEP_OVER, STEP_OUT -> state.getCallStack().size() <= stepTargetDepth;
              case NONE -> false;
            };

        boolean shouldPauseNow = depthConditionMet && !onSameLine;

        if (shouldPauseNow) {
          stepMode = StepMode.NONE;
          stepOriginLocation = Location.UNKNOWN;
          // Notify the debugger that the step completed (so it can send a "step" stopped event
          // and update the UI), then unconditionally block until resumed.  We do this here
          // rather than deferring to the next iteration's pre-execution hook so the pause
          // happens even when the op-stack is now empty (end of program).
          if (debugger != null) {
            debugger.onStep(nextOp != null ? nextOp : currentOp, nextLocation);
          }
          // Always pause — the step is complete.
          waitForResume();
        }
      }

      lastAction = currentAction;
      ++state.instructionCount;
      return currentAction;
    } catch (Exception e) {
      cleanupAfterAbort();
      return Action.Abort(Optional.of(e), "Error during execution: " + e);
    }
  }

  private void handleStepIntoRegion(Action.StepIntoRegion stepIntoRegion, Operation currentOp) {
    assert state != null;
    currentOp
        .getNext()
        .ifPresentOrElse(
            opStack::push,
            () -> {
              currentOp.emitError(
                  "Reached end of block without an explicit jump or return after stepping into region.");
              throw new IllegalStateException();
            });
    // Push the current op onto the stack so that we can retrieve it when we want to set the
    // return value that
    // the region returns.
    opStack.push(currentOp);
    // Open a new stack frame for the region and jump to the first operation in the region.
    state.pushStackFrame(stepIntoRegion.isolatedFromAbove());
    currentHitBreakpoints.push(Optional.empty());

    // Same as for the func op we need to push the body values of the region onto the stack.
    List<Value> bodyValues = stepIntoRegion.region().getBodyValues();
    setupRegion(state, bodyValues, stepIntoRegion.args());
    opStack.push(stepIntoRegion.region().getEntryOperation());
  }

  private void handleTerminate(Action.Terminate aTerminate) {
    assert state != null;
    state.popStackFrame();
    if (aTerminate.fromCall()) state.popCallStack();
    currentHitBreakpoints.pop();
    Operation caller = opStack.pop();
    if (aTerminate.value() != null) {
      state.setValueForOutput(caller, aTerminate.value());
    }
  }

  private void handleJumpToRegion(Action.JumpToRegion jumpToRegion, Operation currentOp) {
    assert state != null;
    assert currentOp.getParentOperation().equals(jumpToRegion.target().getParent())
        : "Jumping to other region only allowed in the same parent operation.";
    // Remove all the values currently held
    boolean wasIsolated = state.popStackFrame().orElseThrow();
    currentHitBreakpoints.pop();
    state.pushStackFrame(wasIsolated);
    currentHitBreakpoints.push(Optional.empty());
    // Set the values of the region's arguments in the new stack frame. These values are
    // stored as body values in the region.
    List<Value> bodyValues = jumpToRegion.target().getBodyValues();
    setupRegion(state, bodyValues, jumpToRegion.args());
    opStack.push(jumpToRegion.target().getEntryOperation());
  }

  private void handleJumpToBlock(Action.JumpToBlock jumpToBlock) {
    opStack.push(jumpToBlock.target().getOperationsRaw().getFirst());
    // Reset the currently hit breakpoints since we are jumping to another block and might hit
    // the same breakpoint again.
    currentHitBreakpoints.pop();
    currentHitBreakpoints.push(Optional.empty());
  }

  private void handleCall(Action.Call call, Operation currentOp) {
    assert state != null;
    // Push the next operation beneath the call operation to the op stack.
    // This way when returning from the function, the VM will know which operation to execute
    // next.
    currentOp.getNext().ifPresent(opStack::push);
    // Push the current op onto the stack so that we can retrieve it when we want to set the
    // return value of the function.
    opStack.push(currentOp);
    state.pushStackFrame(true);
    currentHitBreakpoints.push(Optional.empty());

    Operation funcOp = call.funcOp();
    // Set the values of the function's arguments in the new stack frame.
    // These values are stored as body values in the function's region.'
    List<Value> bodyValues = funcOp.getFirstRegionOrThrow().getBodyValues();
    setupRegion(state, bodyValues, call.args());
    state.pushCallStack(funcOp);
    opStack.push(funcOp.getFirstRegionOrThrow().getEntryOperation());
  }

  /**
   * Helper for determining whether the next operation is still on the same breakpoint as the
   * current one. This is used to prevent firing the same breakpoint multiple times when stepping
   * through an op that matches a breakpoint (e.g. because it has multiple source locations). The
   * set of currently hit breakpoints is tracked in {@link #currentHitBreakpoints} and this method
   * checks whether the next operation belongs to the same breakpoint as the current one by
   * comparing the source location of the next operation with the location of the breakpoint. If the
   * breakpoint is column sensitive, it also checks the column of the next operation to make sure we
   * are not on another breakpoint on the same line.
   *
   * @param onSameLine whether the next operation is on the same line as the current one.
   * @param nextLocation the source location of the next operation.
   * @return true if the next operation is on the same breakpoint as the current one,
   */
  private boolean isOnSameBreakpoint(boolean onSameLine, Location nextLocation) {
    boolean onSameBreakpoint = onSameLine;
    assert !currentHitBreakpoints.isEmpty();
    Optional<Breakpoint> currentBreakpoint = currentHitBreakpoints.peek();
    // If the breakpoint is column sensitive check the column of the next operation as well to
    // check if we are on another breakpoint.
    if (onSameLine
        && currentBreakpoint.isPresent()
        && currentBreakpoint.get().getColumn() != null) {
      onSameBreakpoint =
          currentBreakpoint.get().getColumn().equals(nextLocation.column())
              && currentBreakpoint.get().getLine() == nextLocation.line();
    }
    return onSameBreakpoint;
  }

  // =========================================================================
  // Private helpers
  // =========================================================================

  /**
   * Block the calling thread until {@link #resume()} or {@link #stepOver()} signals it. Interrupted
   * exceptions are re-wrapped as {@link RuntimeException} so the outer try/catch in {@link #step()}
   * can abort cleanly.
   */
  private void waitForResume() {
    pauseLock.lock();
    try {
      paused = true;
      while (paused) {
        resumeCondition.await();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("VM execution interrupted while paused", e);
    } finally {
      pauseLock.unlock();
    }
  }

  private static void setupRegion(
      @NotNull State state, @NotNull List<Value> bodyValues, @NotNull List<Object> args) {
    assert bodyValues.size() == args.size()
        : "Number of arguments does not match number of body values.";
    for (int i = 0; i < bodyValues.size(); i++) {
      state.setValue(bodyValues.get(i), args.get(i));
    }
  }

  protected @NotNull Action stepImpl() {
    assert program != null : "VM not initialized with a program.";
    assert !opStack.isEmpty() : "No operation to execute.";
    assert state != null : "No state to execute the operation in.";

    Operation currentOp = opStack.pop();
    var runnerOpt = OpRunnerRegistry.getOpRunner(currentOp);
    assert runnerOpt.isPresent()
        : "No runner registered for operation " + currentOp.getDetails().ident();

    return runnerOpt.get().run(currentOp, state);
  }

  private void cleanupAfterAbort() {
    opStack.clear();
    if (state != null) {
      state.reset();
    }
  }
}

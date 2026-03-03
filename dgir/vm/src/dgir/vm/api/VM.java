package dgir.vm.api;

import static dialect.builtin.BuiltinOps.*;

import core.debug.Location;
import core.ir.Operation;
import core.ir.Value;
import core.traits.INoTerminator;
import dgir.vm.dap.DebugUtils;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public class VM {
  private @Nullable ProgramOp program;
  private @Nullable State state;
  private @Nullable Action lastAction;

  private final @NotNull Deque<Operation> callStack = new ArrayDeque<>();
  private final @NotNull Deque<Operation> opStack = new ArrayDeque<>();

  // =========================================================================
  // Debug support
  // =========================================================================

  /** Optional debugger callback; {@code null} means debug mode is off. */
  private @Nullable Debugger debugger;

  /** Active breakpoints. Only consulted when a {@link Debugger} is attached. */
  private final @NotNull Set<Breakpoint> breakpoints = new HashSet<>();

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
    STEP_OVER
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
    callStack.clear();
    opStack.clear();
    opStack.push(program.getOperation());
    lastAction = null;
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
    Operation top = opStack.peek();
    return top == null ? Location.UNKNOWN : top.getLocation();
  }

  /**
   * Returns a snapshot of the current operation stack from top (innermost) to bottom (outermost).
   *
   * <p>This may be called from any thread while the VM is paused to populate the DAP {@code
   * StackTraceResponse}. Each entry is a pending {@link Operation}; entries that are call-site
   * markers (operations whose next op was pushed beneath them) are included so the debugger can
   * show a meaningful call chain.
   *
   * @return an unmodifiable list of operations, innermost first.
   */
  public @NotNull @Unmodifiable List<Operation> getCallStack() {
    return List.copyOf(callStack);
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
    pauseLock.lock();
    try {
      paused = false;
      stepMode = StepMode.STEP_OVER;
      // Record the current callstack depth so we can detect when we have returned to
      // the same "level". The op currently on top will be executed next (and may push
      // nested ops); we want to pause once the stack is back at this depth.
      stepTargetDepth = callStack.size();
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

    return true;
  }

  public @NotNull Action step() {
    try {
      assert !opStack.isEmpty() : "No operation to execute.";
      assert state != null : "No state to execute the operation in.";

      Operation currentOp = opStack.peek();
      assert currentOp != null : "Reached end of program without an explicit jump or return.";

      // We reached the end of the program. This is a special case since the operation will not
      // push a next operation onto the stack and we would loop endlessly if we did not stop here.
      if (currentOp.hasTrait(INoTerminator.class) && lastAction instanceof Action.Terminate) {
        callStack.pop();
        opStack.pop();
        return Action.Next();
      }

      // ------------------------------------------------------------------
      // Debug hook: notify the debugger before executing the operation.
      // ------------------------------------------------------------------
      if (debugger != null) {
        Location location = currentOp.getLocation();

        // Check breakpoints first; only fire the first matching one per step.
        // Breakpoints always interrupt execution, even during a step-over.
        for (Breakpoint bp : breakpoints) {
          if (DebugUtils.breakpointMatches(bp, location)) {
            DebugControl bpCtrl = debugger.onBreakpointHit(currentOp, bp, location);
            if (bpCtrl == DebugControl.PAUSE) {
              // A breakpoint hit cancels any pending step mode so the prior step context
              // does not bleed into the next user command.
              stepMode = StepMode.NONE;
              stepOriginLocation = Location.UNKNOWN;
              // Block until the user issues continue/next/stepIn.
              // Important: currentOp is still on opStack (it was peek()ed, not pop()ed), so
              // any stepOver()/stepIn() call that arrives while we wait here will read
              // getCurrentLocation() == currentOp.getLocation(). That means stepOriginLocation
              // is set to the breakpointed op's line, which is exactly right: the subsequent
              // step will execute the breakpointed op and then advance to the next source line,
              // skipping any peer IR ops that share the same line as the breakpoint.
              waitForResume();
            }
            break;
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
        case Action.Call call -> {
          // Push the next operation beneath the call operation to the op stack.
          // This way when returning from the function, the VM will know which operation to execute
          // next.
          currentOp.getNext().ifPresent(opStack::push);
          // Push the current op onto the stack so that we can retrieve it when we want to set the
          // return value of the function.
          opStack.push(currentOp);
          state.pushStackFrame(true);

          Operation funcOp = call.funcOp();
          // Set the values of the function's arguments in the new stack frame.
          // These values are stored as body values in the function's region.'
          List<Value> bodyValues = funcOp.getFirstRegion().orElseThrow().getBodyValues();
          setupRegion(state, bodyValues, call.args());
          callStack.push(funcOp);
          opStack.push(funcOp.getFirstRegion().get().getEntryOperation());
        }
        // Jump to another block in the same region. This is used for control flow operations like
        // if and while.
        case Action.JumpToBlock jumpToBlock -> {
          opStack.push(jumpToBlock.target().getOperations().getFirst());
        }
        // Jump to another region in the same block. This is used for control flow operations like
        // while which have a
        // separate region for the body and the condition check logic.
        case Action.JumpToRegion jumpToRegion -> {
          // Remove all the values currently held
          var oldFrame = state.popStackFrame().orElseThrow();
          state.pushStackFrame(oldFrame.getRight());
          // Set the values of the region's arguments in the new stack frame. These values are
          // stored as body values in the region.
          List<Value> bodyValues = jumpToRegion.target().getBodyValues();
          setupRegion(state, bodyValues, jumpToRegion.args());
          opStack.push(jumpToRegion.target().getEntryOperation());
        }
        // Return from the current region. This is used for function calls, as well as for returning
        // from if and while
        // blocks and similar structured control flow ops
        case Action.Terminate aTerminate -> {
          state.popStackFrame();
          Operation caller = opStack.pop();
          if (aTerminate.value() != null) {
            state.setValueForOutput(caller, aTerminate.value());
          }
        }
        // Step into a region. This is used for nested regions like the then and else regions of an
        // if operation, or the
        // body of a while operation, as well as function calls.
        // It opens a new stack frame for the region and jumps to the first operation in the region.
        case Action.StepIntoRegion stepIntoRegion -> {
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

          // Same as for the func op we need to push the body values of the region onto the stack.
          List<Value> bodyValues = stepIntoRegion.region().getBodyValues();
          setupRegion(state, bodyValues, stepIntoRegion.args());
          opStack.push(stepIntoRegion.region().getEntryOperation());
        }
      }

      // If we did a single-step, decide whether to pause now.
      if (stepMode != StepMode.NONE) {
        Operation nextOp = opStack.peek();
        Location nextLocation = nextOp != null ? nextOp.getLocation() : currentOp.getLocation();

        // Depth condition: have we returned to the level we started stepping at?
        boolean depthConditionMet =
            switch (stepMode) {
              // Step-in: depth is always satisfied after any single operation.
              case STEP_IN -> true;
              // Step-over: only once the call-stack depth is back at (or below) the target.
              case STEP_OVER -> callStack.size() <= stepTargetDepth;
              case NONE -> false;
            };

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
    OpRunner runner = OpRunnerRegistry.getOpRunner(currentOp);
    assert runner != null : "No runner registered for operation " + currentOp.getDetails().ident();

    return runner.run(currentOp, state);
  }

  private void cleanupAfterAbort() {
    opStack.clear();
    if (state != null) {
      state.reset();
    }
  }
}

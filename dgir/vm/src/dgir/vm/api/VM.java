package dgir.vm.api;

import core.ir.Operation;
import core.ir.SourceLocation;
import core.ir.Value;
import core.traits.INoTerminator;
import dialect.builtin.ProgramOp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class VM {
  private @Nullable ProgramOp program;
  private @Nullable State state;
  private @Nullable Action lastAction;

  private final @NotNull Deque<Operation> opStack = new ArrayDeque<>();

  // =========================================================================
  // Debug support
  // =========================================================================

  /** Optional debugger callback; {@code null} means debug mode is off. */
  private @Nullable Debugger debugger;

  /** Active breakpoints. Only consulted when a {@link Debugger} is attached. */
  private final @NotNull Set<Breakpoint> breakpoints = new HashSet<>();

  /**
   * Pause/resume lock. When a debugger callback returns {@link DebugControl#PAUSE} the VM
   * thread waits on {@link #resumeCondition}; {@link #resume()} or {@link #stepOver()} signals it.
   */
  private final @NotNull ReentrantLock pauseLock = new ReentrantLock();
  private final @NotNull Condition resumeCondition = pauseLock.newCondition();
  private volatile boolean paused = false;

  /**
   * When {@code true} the VM pauses again after executing exactly one more operation.
   * Set by {@link #stepOver()}, cleared once consumed.
   */
  private volatile boolean stepOnce = false;

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
  }

  // =========================================================================
  // Debug API
  // =========================================================================

  /**
   * Attach a {@link Debugger} to this VM. While a debugger is attached the VM calls
   * {@link Debugger#onStep} (and {@link Debugger#onBreakpointHit} when applicable) before each
   * operation is dispatched. Pass {@code null} to detach and disable debug mode.
   *
   * @param debugger the debugger to attach, or {@code null} to disable debug mode.
   */
  public void setDebugger(@Nullable Debugger debugger) {
    this.debugger = debugger;
  }

  /**
   * Returns the currently attached {@link Debugger}, or {@code Optional.empty()} if none is attached.
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
   * Return the {@link SourceLocation} of the operation that would be executed next, or
   * {@link SourceLocation#UNKNOWN} if the stack is empty or the VM has not been initialised.
   *
   * <p>This may be called from any thread (including while the VM is paused) to obtain the
   * current program counter for DAP {@code StoppedEvent} / {@code StackTraceResponse} payloads.
   *
   * @return the current source location.
   */
  public @NotNull SourceLocation getCurrentLocation() {
    if (opStack.isEmpty()) return SourceLocation.UNKNOWN;
    Operation top = opStack.peek();
    return top == null ? SourceLocation.UNKNOWN : top.getLocation();
  }

  /**
   * Resume a paused VM. Unblocks the VM thread waiting inside {@link #run()} or {@link #step()}.
   * Calling this when the VM is not paused is a no-op.
   */
  public void resume() {
    pauseLock.lock();
    try {
      paused = false;
      stepOnce = false;
      resumeCondition.signalAll();
    } finally {
      pauseLock.unlock();
    }
  }

  /**
   * Advance exactly one operation from a paused state and then pause again. Implements the DAP
   * "next" / "stepOver" command at IR level (every operation is one step).
   * Calling this when the VM is not paused is a no-op.
   */
  public void stepOver() {
    pauseLock.lock();
    try {
      paused = false;
      stepOnce = true;
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
        opStack.pop();
        return Action.Next();
      }

      // ------------------------------------------------------------------
      // Debug hook: notify the debugger before executing the operation.
      // ------------------------------------------------------------------
      if (debugger != null) {
        SourceLocation location = currentOp.getLocation();

        // Check breakpoints first; only fire the first matching one per step.
        for (Breakpoint bp : breakpoints) {
          if (bp.matches(location)) {
            DebugControl bpCtrl = debugger.onBreakpointHit(currentOp, bp, location);
            if (bpCtrl == DebugControl.PAUSE) {
              waitForResume();
            }
            break;
          }
        }

        // Always call onStep so the adapter can update its UI.
        DebugControl stepCtrl = debugger.onStep(currentOp, location);
        if (stepCtrl == DebugControl.PAUSE) {
          waitForResume();
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
          opStack.push(funcOp.getFirstRegion().get().getEntryOperation());
        }
        // Jump to another block in the same region. This is used for control flow operations like
        // if and while.
        case Action.Jump jump -> {
          opStack.push(jump.target().getOperations().getFirst());
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
        case Action.StepInto stepInto -> {
          currentOp
              .getNext()
              .ifPresentOrElse(
                  opStack::push,
                  () -> {
                    currentOp.emitError(
                        "Reached end of block without an explicit jump or return after stepping into region.");
                    throw new IllegalStateException();
                  });
          // Push the current op onto the stack so that we can retrieve it when we want to set the return value that
          // the region returns.
          opStack.push(currentOp);
          // Open a new stack frame for the region and jump to the first operation in the region.
          state.pushStackFrame(stepInto.isolatedFromAbove());

          // Same as for the func op we need to push the body values of the region onto the stack.
          List<Value> bodyValues = stepInto.region().getBodyValues();
          setupRegion(state, bodyValues, stepInto.args());
          opStack.push(stepInto.region().getEntryOperation());
        }
      }

      // If we did a single-step, re-arm the pause for the next operation.
      if (stepOnce) {
        pauseLock.lock();
        try {
          stepOnce = false;
          paused = true;
        } finally {
          pauseLock.unlock();
        }
      }

      lastAction = currentAction;
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
   * Block the calling thread until {@link #resume()} or {@link #stepOver()} signals it.
   * Interrupted exceptions are re-wrapped as {@link RuntimeException} so the outer try/catch in
   * {@link #step()} can abort cleanly.
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
    assert runner != null
        : "No runner registered for operation " + currentOp.getDetails().ident();

    return runner.run(currentOp, state);
  }

  private void cleanupAfterAbort() {
    opStack.clear();
    if (state != null) {
      state.reset();
    }
  }
}

package dgir.vm.api;

import core.ir.Operation;
import core.ir.Value;
import core.traits.INoTerminator;
import dialect.builtin.ProgramOp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class VM {
  private @Nullable ProgramOp program;
  private @Nullable State state;
  private @Nullable Action lastAction;

  private @NotNull Deque<Operation> opStack = new ArrayDeque<>();

  public VM() {

  }

  public void init(@NotNull ProgramOp program) {
    assert program.verify(true) : "Program is invalid.";
    this.program = program;
    this.opStack.clear();
    this.opStack.push(program.get());
    this.state = new State();
  }

  public boolean run() {
    if (program == null) {
      System.err.println("VM not initialized with a program.");
      return false;
    }

    Action currentAction = Action.Next();
    while (!(currentAction instanceof Action.Abort) && !opStack.isEmpty()) {
      currentAction = step();
    }

    return !(currentAction instanceof Action.Abort);
  }

  public @NotNull Action step() {
    try {
      assert !opStack.isEmpty() : "No operation to execute.";
      assert state != null : "No state to execute the operation in.";

      Operation currentOp = opStack.peek();
      assert currentOp != null : "Reached end of program without an explicit jump or return.";

      // We reached the end of the program. This is a special case since the operation will not push a next operation onto the stack
      // and we would cause and endless loop if we did not terminate like this.
      if (currentOp.hasTrait(INoTerminator.class) && lastAction instanceof Action.Terminate) {
        opStack.pop();
        return Action.Next();
      }

      Action currentAction = stepImpl();
      switch (currentAction) {
        // Just continue to the next operation in the current block.
        case Action.Next ignored -> {
          currentOp.getNext().ifPresentOrElse(opStack::push, () -> {
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
          // This way when returning from the function, the VM will know which operation to execute next.
          currentOp.getNext().ifPresent(opStack::push);
          // Push the current op onto the stack so that we can retrieve it when we want to set the return value of the function.
          opStack.push(currentOp);
          state.pushStackFrame(true);

          Operation funcOp = call.funcOp();
          // Set the values of the function's arguments in the new stack frame.
          // These values are stored as body values in the function's region.'
          List<Value> bodyValues = funcOp.getFirstRegion().orElseThrow().getBodyValues();
          setupRegion(state, bodyValues, call.args());

          // Push the first operation in the function's region onto the op stack.'
          opStack.push(funcOp.getFirstRegion().get().getEntryOperation());
        }
        // Jump to another block in the same region. This is used for control flow operations like if and while.
        case Action.Jump jump -> {
          opStack.push(jump.target().getOperations().getFirst());
        }
        // Return from the current region. This is used for function calls, as well as for returning from if and while
        // blocks and similar structured control flow ops
        case Action.Terminate aTerminate -> {
          // Pop the stack frame for the region we just left.
          state.popStackFrame();

          // Set the return value of the call operation if it produces any.
          Operation caller = opStack.pop();
          if (aTerminate.value() != null) {
            state.setValueForOutput(caller, aTerminate.value());
          }
          // No need to push anything to the op stack, as the caller will have already pushed the next operation to execute after the call.
        }
        // Step into a region. This is used for nested regions like the then and else regions of an if operation, or the
        // body of a while operation, as well as function calls.
        // It opens a new stack frame for the region and jumps to the first operation in the region.
        case Action.StepInto stepInto -> {
          // Push the next operation after the step into operation to the op stack.
          stepInto.nextOperation().ifPresent(opStack::push);
          // Open a new stack frame for the region and jump to the first operation in the region.
          state.pushStackFrame(stepInto.isolatedFromAbove());

          // Same as for the func op we need to push the body values of the region onto the stack.
          List<Value> bodyValues = stepInto.region().getBodyValues();
          setupRegion(state, bodyValues, stepInto.args());

          opStack.push(stepInto.region().getEntryOperation());

        }
      }

      lastAction = currentAction;
      return currentAction;
    } catch (Exception e) {
      System.err.println("Error during execution: " + e.getMessage());
      e.printStackTrace(System.err);
      cleanupAfterAbort();
      return Action.Abort("Error during execution: " + e);
    }
  }

  private static void setupRegion(@NotNull State state, @NotNull List<Value> bodyValues, @NotNull List<Object> args) {
    assert bodyValues.size() == args.size() : "Number of arguments does not match number of body values.";
    for (int i = 0; i < bodyValues.size(); i++) {
      Value argValue = bodyValues.get(i);
      Object argObject = args.get(i);
      state.setValue(argValue, argObject);
    }
  }

  protected @NotNull Action stepImpl() {
    assert program != null : "VM not initialized with a program.";
    assert !opStack.isEmpty() : "No operation to execute.";
    assert state != null : "No state to execute the operation in.";

    Operation currentOp = opStack.pop();
    OpRunner runner = OpRunnerRegistry.getOpRunner(currentOp);
    assert runner != null : "No runner registered for operation " + currentOp.getDetails().getIdent();

    return runner.run(currentOp, state);
  }

  private void cleanupAfterAbort() {
    opStack.clear();
    if (state != null) {
      state.reset();
    }
  }
}

package dgir.vm.api;

import core.ir.Op;
import core.ir.Operation;
import dialect.builtin.ProgramOp;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VM {
  public enum Status {
    OK,
    ERROR,
    FINISHED
  }

  static class StepResult {
    @NotNull Status status;
    @Nullable Operation nextOperation;
    @NotNull String message;

    StepResult(@NotNull Status status, @Nullable Operation nextOperation, @NotNull String message) {
      this.status = status;
      this.nextOperation = nextOperation;
      this.message = message;
    }
  }

  private @Nullable ProgramOp program;

  public VM() {

  }

  public void init(@NotNull ProgramOp program) {
    this.program = program;
  }

  public boolean run() {
    if (program == null) {
      System.err.println("VM not initialized with a program.");
      return false;
    }

    StepResult result;
    do {
      result = stepImpl();
    } while (result.status == Status.OK);

    if (result.status == Status.FINISHED) {
      System.out.println("Program finished successfully.");
      return true;
    } else {
      System.err.println("Program failed with error: " + result.message);
      return false;
    }
  }

  private @NotNull StepResult stepImpl() {
    return new StepResult(Status.ERROR, null, "Not implemented yet");
  }

  public @NotNull Pair<Status, String> step() {
    return Pair.of(Status.ERROR, "Not implemented yet");
  }
}

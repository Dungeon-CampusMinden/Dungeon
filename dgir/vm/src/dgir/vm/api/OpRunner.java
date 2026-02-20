package dgir.vm.api;

import core.detail.OperationDetails;
import core.ir.Operation;
import org.jetbrains.annotations.NotNull;

public abstract class OpRunner {
  private final @NotNull OperationDetails targetOp;

  public OpRunner(@NotNull OperationDetails targetOp) {
    this.targetOp = targetOp;
  }

  public final @NotNull OperationDetails getTargetOp() {
    return targetOp;
  }

  public final @NotNull Action run(@NotNull Operation op, @NotNull State state) {
    if (!op.getDetails().equals(targetOp)) {
      throw new IllegalArgumentException(
          "OpRunner can only run operations of type "
              + targetOp.getIdent()
              + ", but got "
              + op.getDetails().getIdent());
    }
    return runImpl(op, state);
  }

  protected abstract @NotNull Action runImpl(@NotNull Operation op, @NotNull State state);
}

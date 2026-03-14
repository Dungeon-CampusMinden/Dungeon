package dgir.vm.api;

import dgir.core.ir.OperationDetails;
import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import org.jetbrains.annotations.NotNull;

public abstract class OpRunner {
  private final @NotNull OperationDetails targetOp;

  public OpRunner(@NotNull Class<? extends Op> targetOpType) {
    this(
        OperationDetails.Registered.lookup(targetOpType)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No OperationDetails registered for " + targetOpType)));
  }

  public OpRunner(@NotNull OperationDetails targetOp) {
    this.targetOp = targetOp;
  }

  public final @NotNull OperationDetails getTargetOp() {
    return targetOp;
  }

  public void clearsState() {}

  public final @NotNull Action run(@NotNull Operation op, @NotNull State state) {
    if (!op.getDetails().equals(targetOp)) {
      throw new IllegalArgumentException(
          "OpRunner can only run operations of type "
              + targetOp.ident()
              + ", but got "
              + op.getDetails().ident());
    }
    return runImpl(op, state);
  }

  protected abstract @NotNull Action runImpl(@NotNull Operation op, @NotNull State state);
}

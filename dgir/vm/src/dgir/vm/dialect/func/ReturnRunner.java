package dgir.vm.dialect.func;

import core.detail.RegisteredOperationDetails;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.func.ReturnOp;
import org.jetbrains.annotations.NotNull;

public class ReturnRunner extends OpRunner {
  public ReturnRunner() {
    super(RegisteredOperationDetails.get(ReturnOp.class));
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    return Action.Return(op.getOutput() != null ? op.getOutputValue() : null);
  }
}

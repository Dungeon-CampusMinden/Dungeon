package dgir.vm.dialect.func;

import core.detail.RegisteredOperationDetails;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.func.FuncOp;
import org.jetbrains.annotations.NotNull;

public class FuncRunner extends OpRunner {
  public FuncRunner() {
    super(RegisteredOperationDetails.get(FuncOp.class));
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    return Action.StepInto(op.getFirstRegion(), true);
  }
}

package dgir.vm.dialect.func;

import core.detail.RegisteredOperationDetails;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.func.FuncOp;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FuncRunner extends OpRunner {
  public FuncRunner() {
    super(RegisteredOperationDetails.lookup(FuncOp.class).orElseThrow());
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    return Action.StepInto(op.getFirstRegion().orElseThrow(), true, Optional.empty());
  }
}

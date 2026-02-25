package dgir.vm.dialect.scf;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.scf.ScopeOp;
import org.jetbrains.annotations.NotNull;

public class ScopeRunner extends OpRunner {
  public ScopeRunner() {
    super(ScopeOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    // Just step into the scope region.
    return Action.StepIntoRegion(op.getRegion(0).orElseThrow(), false);
  }
}

package dgir.vm.dialect.scf;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import static dialect.scf.ScfOps.BreakOp;

public class BreakRunner extends OpRunner {
  public BreakRunner() {
    super(BreakOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    // Just terminate the current region.
    return Action.Terminate(null);
  }
}

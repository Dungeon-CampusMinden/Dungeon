package dgir.vm.dialect.scf;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.scf.BreakOp;
import org.jetbrains.annotations.NotNull;

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

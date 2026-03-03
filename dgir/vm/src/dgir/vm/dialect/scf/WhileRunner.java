package dgir.vm.dialect.scf;

import static dialect.scf.ScfOps.*;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

public class WhileRunner extends OpRunner {
  public WhileRunner() {
    super(WhileOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    WhileOp whileOp = op.as(WhileOp.class).orElseThrow();
    return Action.StepIntoRegion(whileOp.getConditionRegion(), false);
  }
}

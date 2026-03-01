package dgir.vm.dialect.scf;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import static dialect.scf.ScfOps.IfOp;

public class IfRunner extends OpRunner {
  public IfRunner() {
    super(IfOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    IfOp ifOp = op.as(IfOp.class).orElseThrow();
    byte condition = state.getValue(ifOp.getOperand(0).orElseThrow(), Byte.class).orElseThrow();
    // Step into the then region if the condition is true, otherwise step into the else region if it exists, or just
    // continue to the next operation.
    if (condition != 0) {
      return Action.StepIntoRegion(ifOp.getThenRegion(), false);
    } else if (ifOp.getElseRegion().isPresent()) {
      return Action.StepIntoRegion(ifOp.getElseRegion().get(), false);
    }
    return Action.Next();
  }
}

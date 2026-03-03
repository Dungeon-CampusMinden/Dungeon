package dgir.vm.dialect.builtin;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.builtin.BuiltinOps;
import org.jetbrains.annotations.NotNull;

public class IdRunner extends OpRunner {
  public IdRunner() {
    super(BuiltinOps.IdOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    BuiltinOps.IdOp idOp = op.as(BuiltinOps.IdOp.class).orElseThrow();
    state.setValueForOutput(op, state.getValue(idOp.getOperand()));
    return Action.Next();
  }
}

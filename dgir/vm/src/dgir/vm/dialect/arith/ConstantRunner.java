package dgir.vm.dialect.arith;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import static dialect.arith.ArithOps.*;

public class ConstantRunner extends OpRunner {
  public ConstantRunner() {
    super(ConstantOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    ConstantOp constantOp = op.as(ConstantOp.class).orElseThrow();
    state.setValue(constantOp.getValue(), constantOp.getValueStorage());
    return Action.Next();
  }
}

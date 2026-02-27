package dgir.vm.dialect.arith;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.arith.CastOp;
import org.jetbrains.annotations.NotNull;

public class CastRunner extends OpRunner {
  public CastRunner() {
    super(CastOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    CastOp castOp = op.as(CastOp.class).orElseThrow();
    var value = NumericUtils.getNumber(state, castOp.getOperand());
    var targetType = castOp.getTargetType();
    var converted = NumericUtils.convertToType(value, targetType);
    state.setValueForOutput(op, converted);
    return Action.Next();
  }
}


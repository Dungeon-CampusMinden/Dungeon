package dgir.vm.dialect.arith;

import core.detail.RegisteredOperationDetails;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.arith.ConstantOp;
import org.jetbrains.annotations.NotNull;

public class ConstantRunner extends OpRunner {
  public ConstantRunner()
  {
    super(RegisteredOperationDetails.get(ConstantOp.class));
  }


  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    ConstantOp constantOp = op.as(ConstantOp.class);
    state.setValue(constantOp.getOutputValue(), constantOp.getValue());
    return Action.Next();
  }
}

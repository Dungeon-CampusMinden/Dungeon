package dgir.vm.dialect.func;

import core.detail.RegisteredOperationDetails;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.func.ReturnOp;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ReturnRunner extends OpRunner {
  public ReturnRunner() {
    super(RegisteredOperationDetails.lookup(ReturnOp.class).orElseThrow());
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    if (op.getOperands().isEmpty()) {
      return Action.Terminate(Optional.empty());
    }
    return Action.Terminate(state.getValue(op.getOperands().getFirst().getValue()));
  }
}

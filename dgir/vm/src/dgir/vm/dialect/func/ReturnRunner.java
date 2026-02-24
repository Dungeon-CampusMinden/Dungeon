package dgir.vm.dialect.func;

import core.ir.Operation;
import core.ir.Value;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.func.ReturnOp;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class ReturnRunner extends OpRunner {
  public ReturnRunner() {
    super(ReturnOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    ReturnOp returnOp = op.as(ReturnOp.class).orElseThrow();
    Optional<Value> returnValue = returnOp.getReturnValue();
    return returnValue
        .map(value -> Action.Terminate(state.getValue(value)))
        .orElseGet(() -> Action.Terminate(null));
  }
}

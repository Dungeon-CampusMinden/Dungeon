package dgir.vm.dialect.func;

import core.ir.Operation;
import core.ir.Value;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static dialect.func.FuncOps.ReturnOp;

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

package dgir.vm.dialect.func;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import static dialect.func.FuncOps.FuncOp;

public class FuncRunner extends OpRunner {
  public FuncRunner() {
    super(FuncOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    return Action.Next();
  }
}

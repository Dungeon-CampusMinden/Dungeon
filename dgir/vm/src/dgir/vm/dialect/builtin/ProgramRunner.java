package dgir.vm.dialect.builtin;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import static dialect.builtin.BuiltinOps.ProgramOp;

public class ProgramRunner extends OpRunner {
  public ProgramRunner() {
    super(ProgramOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    ProgramOp programOp = op.as(ProgramOp.class).orElseThrow();
    return Action.Call(programOp.getMainFunc().getOperation());
  }
}

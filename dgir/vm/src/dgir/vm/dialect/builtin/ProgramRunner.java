package dgir.vm.dialect.builtin;

import core.detail.RegisteredOperationDetails;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.builtin.ProgramOp;
import org.jetbrains.annotations.NotNull;

public class ProgramRunner extends OpRunner {
  public ProgramRunner() {
    super(RegisteredOperationDetails.lookup(ProgramOp.class).orElseThrow());
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    ProgramOp programOp = op.as(ProgramOp.class).orElseThrow();
    return Action.Call(programOp.getMainFunc().get());
  }
}

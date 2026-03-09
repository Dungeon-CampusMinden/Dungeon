package dgir.vm.dialect.builtin;

import dgir.core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dgir.dialect.builtin.BuiltinOps;
import org.jetbrains.annotations.NotNull;

public sealed interface BuiltinRunners {
  final class IdRunner extends OpRunner implements BuiltinRunners {
    public IdRunner() {
      super(BuiltinOps.IdOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      state.setValueForOutput(op, state.getValue(op.getOperand(0).orElseThrow()));
      return Action.Next();
    }
  }

  final class ProgramRunner extends OpRunner implements BuiltinRunners {
    public ProgramRunner() {
      super(BuiltinOps.ProgramOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      BuiltinOps.ProgramOp programOp = op.as(BuiltinOps.ProgramOp.class).orElseThrow();
      return Action.Call(programOp.getMainFunc().getOperation());
    }
  }
}

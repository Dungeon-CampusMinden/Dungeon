package dgir.vm.dialect.builtin;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.builtin.BuiltinOps;
import org.jetbrains.annotations.NotNull;

public sealed interface BuiltinRunners {
  final class IdRunner extends OpRunner implements BuiltinRunners {
    public IdRunner() {
      super(BuiltinOps.IdOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      BuiltinOps.IdOp idOp = op.as(BuiltinOps.IdOp.class).orElseThrow();
      state.setValueForOutput(op, state.getValue(idOp.getOperand()));
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

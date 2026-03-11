package blockly.dgir.vm.dialect.dg;

import blockly.dgir.dialect.dg.DgOps;
import dgir.core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

public sealed interface DgRunners {
  final class MoveRunner extends OpRunner implements DgRunners {
    public MoveRunner() {
      super(DgOps.MoveOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      return Action.Next();
    }
  }
}

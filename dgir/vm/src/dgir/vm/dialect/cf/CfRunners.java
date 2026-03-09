package dgir.vm.dialect.cf;

import dgir.core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dgir.dialect.cf.CfOps;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public sealed interface CfRunners {
  final class BranchCondRunner extends OpRunner implements CfRunners {
    public BranchCondRunner() {
      super(CfOps.BranchCondOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      byte condition = state.getValueAsOrThrow(op.getOperandOrThrow(0), Byte.class);
      if (condition != 0) {
        return Action.JumpToBlock(op.getBlockOperands().getFirst().getValueOrThrow());
      } else {
        return Action.JumpToBlock(op.getBlockOperands().get(1).getValueOrThrow());
      }
    }
  }

  final class BranchRunner extends OpRunner implements CfRunners {
    public BranchRunner() {
      super(CfOps.BranchOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      return Action.JumpToBlock(op.getSuccessors().getFirst());
    }
  }

  final class AssertRunner extends OpRunner implements CfRunners {
    public AssertRunner() {
      super(CfOps.AssertOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      byte condition = state.getValueAsOrThrow(op.getOperandOrThrow(0), Byte.class);
      if (condition == 0) {
        if (op.getOperand(1).isPresent()) {
          String message = state.getValueAsOrThrow(op.getOperand(1).get(), String.class);
          return Action.Abort(Optional.empty(), op.getLocation() + " -> " + message);
        } else {
          return Action.Abort(Optional.empty(), op.getLocation() + " -> " + "Assertion failed.");
        }
      }
      return Action.Next();
    }
  }
}

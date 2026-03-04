package dgir.vm.dialect.cf;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.cf.CfOps;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public sealed interface CfRunners {
  final class BranchCondRunner extends OpRunner implements CfRunners {
    public BranchCondRunner() {
      super(CfOps.BranchCondOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      CfOps.BranchCondOp branchCondOp = op.as(CfOps.BranchCondOp.class).orElseThrow();
      byte condition =
          state.getValue(branchCondOp.getOperand(0).orElseThrow(), Byte.class).orElseThrow();
      if (condition != 0) {
        return Action.JumpToBlock(op.getSuccessors().get(0));
      } else {
        return Action.JumpToBlock(op.getSuccessors().get(1));
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
      CfOps.AssertOp assertOp = op.as(CfOps.AssertOp.class).orElseThrow();
      byte condition =
          state.getValue(assertOp.getOperand(0).orElseThrow(), Byte.class).orElseThrow();
      if (condition == 0) {
        if (assertOp.getOperand(1).isPresent()) {
          String message = state.getValue(assertOp.getOperand(1).get(), String.class).orElseThrow();
          return Action.Abort(Optional.empty(), assertOp.getLocation() + " -> " + message);
        } else {
          return Action.Abort(
              Optional.empty(), assertOp.getLocation() + " -> " + "Assertion failed.");
        }
      }
      return Action.Next();
    }
  }
}

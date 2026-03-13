package dgir.vm.dialect.scf;

import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.dialect.builtin.BuiltinTypes;
import dgir.dialect.scf.ScfOps;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import io.arxila.javatuples.Quartet;
import org.jetbrains.annotations.NotNull;

public sealed interface ScfRunners {
  final class EndRunner extends OpRunner implements ScfRunners {
    public EndRunner() {
      super(ScfOps.EndOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      return Action.Terminate(null, false);
    }
  }

  final class ContinueRunner extends OpRunner implements ScfRunners {
    public ContinueRunner() {
      super(ScfOps.ContinueOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      Operation parentOp = op.getParentOperationOrThrow();
      if (parentOp.isa(ScfOps.ForOp.class)) {
        return handleForOp(parentOp, state);
      } else if (parentOp.isa(ScfOps.WhileOp.class)) {
        return handleWhileOp(op, parentOp, state);
      }
      throw new IllegalStateException("Unexpected parent operation for continue op: " + parentOp);
    }

    public Action handleForOp(Operation forOp, State state) {
      Quartet<Long, Long, Long, Long> bounds = ForRunner.getBounds(forOp, state);
      long lowerBoundNum = bounds.value1();
      long upperBoundNum = bounds.value2();
      long stepNum = bounds.value3();

      Value induction = forOp.getRegionOrThrow(0).getBodyValue(0).orElseThrow();
      long inductionValue = state.getValueAsOrThrow(induction, Long.class);
      boolean breakValue =
          state.getValueAsOrThrow(
              forOp.getRegionOrThrow(0).getBodyValue(1).orElseThrow(), Boolean.class);

      inductionValue += stepNum;
      state.setValue(induction, inductionValue);

      if (inductionValue < upperBoundNum && inductionValue >= lowerBoundNum && !breakValue) {
        return Action.JumpToRegion(forOp.getRegionOrThrow(0), inductionValue, false);
      }

      return Action.Terminate(null, false);
    }

    public Action handleWhileOp(Operation continueOp, Operation whileOp, State state) {
      if (whileOp.getRegionOrThrow(0).equals(continueOp.getParentRegionOrThrow())) {
        return Action.JumpToRegion(
            whileOp.getRegionOrThrow(1),
            state.getValueOrThrow(whileOp.getRegionOrThrow(0).getBodyValue(0).orElseThrow()));
      } else {
        return Action.JumpToRegion(
            whileOp.getRegionOrThrow(0),
            state.getValueOrThrow(whileOp.getRegionOrThrow(1).getBodyValue(0).orElseThrow()));
      }
    }
  }

  final class ForRunner extends OpRunner implements ScfRunners {

    public ForRunner() {
      super(ScfOps.ForOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation forOp, @NotNull State state) {
      Quartet<Long, Long, Long, Long> bounds = getBounds(forOp, state);
      long initialValueNum = bounds.value0();
      long lowerBoundNum = bounds.value1();
      long upperBoundNum = bounds.value2();

      if (initialValueNum < upperBoundNum && initialValueNum >= lowerBoundNum) {
        return Action.StepIntoRegion(
            forOp.getRegionOrThrow(0), false, initialValueNum, BuiltinTypes.IntegerT.FALSE);
      } else {
        return Action.Next();
      }
    }

    public static Quartet<Long, Long, Long, Long> getBounds(Operation forOp, State state) {
      Value initialValue = forOp.getOperandValueOrThrow(0);
      Value lowerBound = forOp.getOperandValueOrThrow(1);
      Value upperBound = forOp.getOperandValueOrThrow(2);
      Value step = forOp.getOperandValueOrThrow(3);

      long initialValueNum = state.getValueAsOrThrow(initialValue, Number.class).longValue();
      long lowerBoundNum = state.getValueAsOrThrow(lowerBound, Number.class).longValue();
      long upperBoundNum = state.getValueAsOrThrow(upperBound, Number.class).longValue();
      long stepNum = state.getValueAsOrThrow(step, Number.class).longValue();

      return new Quartet<>(initialValueNum, lowerBoundNum, upperBoundNum, stepNum);
    }
  }

  final class IfRunner extends OpRunner implements ScfRunners {
    public IfRunner() {
      super(ScfOps.IfOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      byte condition = state.getValueAsOrThrow(op.getOperandOrThrow(0), Byte.class);
      if (condition != 0) {
        return Action.StepIntoRegion(op.getRegionOrThrow(0), false);
      } else if (op.getRegions().size() > 1) {
        return Action.StepIntoRegion(op.getRegionOrThrow(1), false);
      }
      return Action.Next();
    }
  }

  final class ScopeRunner extends OpRunner implements ScfRunners {
    public ScopeRunner() {
      super(ScfOps.ScopeOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      return Action.StepIntoRegion(op.getRegionOrThrow(0), false);
    }
  }

  final class WhileRunner extends OpRunner implements ScfRunners {
    public WhileRunner() {
      super(ScfOps.WhileOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      ScfOps.WhileOp whileOp = op.as(ScfOps.WhileOp.class).orElseThrow();
      return Action.StepIntoRegion(
          whileOp.getConditionRegion(), false, BuiltinTypes.IntegerT.FALSE);
    }
  }
}

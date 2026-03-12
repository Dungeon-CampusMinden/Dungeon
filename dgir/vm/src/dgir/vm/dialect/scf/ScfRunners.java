package dgir.vm.dialect.scf;

import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.dialect.scf.ScfOps;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import io.arxila.javatuples.Quartet;
import org.apache.commons.lang3.tuple.Pair;
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

  private static Pair<Integer, Operation> getDepth(@NotNull Operation op) {
    int depth = 0;
    Operation parent = op.getParentOperation().orElse(null);
    while (parent != null && !parent.isa(ScfOps.WhileOp.class) && !parent.isa(ScfOps.ForOp.class)) {
      parent = parent.getParentOperation().orElse(null);
      depth++;
    }
    if (parent == null) {
      throw new IllegalStateException("Unexpected continue op: " + op);
    }
    return Pair.of(depth, parent);
  }

  final class BreakRunner extends OpRunner implements ScfRunners {
    public BreakRunner() {
      super(ScfOps.BreakOp.class);
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
      Operation parent = op.getParentOperationOrThrow();
      // If we have a for op we need to handle it properly by incrementing the induction variable
      // and
      // checking if we should continue the loop or not.
      if (parent.isa(ScfOps.ForOp.class)) {
        return handleForOp(parent, state);
      } else if (parent.isa(ScfOps.WhileOp.class)) {
        return handleWhileOp(op, parent, state);
      }
      throw new IllegalStateException("Unexpected continue op: " + op);
    }

    public Action handleForOp(Operation forOp, State state) {
      Quartet<Long, Long, Long, Long> bounds = ForRunner.getBounds(forOp, state);
      long lowerBoundNum = bounds.value1();
      long upperBoundNum = bounds.value2();
      long stepNum = bounds.value3();

      Value induction = forOp.getRegionOrThrow(0).getBodyValue(0).orElseThrow();
      long inductionValue = state.getValueAsOrThrow(induction, Long.class);

      inductionValue += stepNum;
      state.setValue(induction, inductionValue);

      if (inductionValue < upperBoundNum && inductionValue >= lowerBoundNum) {
        return Action.JumpToRegion(forOp.getRegionOrThrow(0), inductionValue);
      }

      return Action.Terminate(null, false);
    }

    public Action handleWhileOp(Operation continueOp, Operation whileOp, State state) {
      if (whileOp.getRegionOrThrow(0).equals(continueOp.getParentRegionOrThrow())) {
        return Action.JumpToRegion(whileOp.getRegionOrThrow(1));
      } else {
        return Action.JumpToRegion(whileOp.getRegionOrThrow(0));
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
        return Action.StepIntoRegion(forOp.getRegionOrThrow(0), false, initialValueNum);
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
      return Action.StepIntoRegion(whileOp.getConditionRegion(), false);
    }
  }
}

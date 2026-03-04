package dgir.vm.dialect.scf;

import core.ir.Operation;
import core.ir.Value;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.scf.ScfOps;
import io.arxila.javatuples.Quartet;
import org.jetbrains.annotations.NotNull;

public sealed interface ScfRunners {
  final class BreakRunner extends OpRunner implements ScfRunners {
    public BreakRunner() {
      super(ScfOps.BreakOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      // Just terminate the current region.
      return Action.Terminate(null);
    }
  }

  final class ContinueRunner extends OpRunner implements ScfRunners {
    public ContinueRunner() {
      super(ScfOps.ContinueOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      ScfOps.ContinueOp continueOp = op.as(ScfOps.ContinueOp.class).orElseThrow();
      Operation parentOp = continueOp.getParentOperation().orElseThrow();
      // If we have a for op we need to handle it properly by incrementing the induction variable
      // and
      // checking if we should continue the loop or not.
      if (parentOp.isa(ScfOps.ForOp.class)) {
        ScfOps.ForOp forOp = parentOp.as(ScfOps.ForOp.class).orElseThrow();
        return handleForOp(forOp, state);
      } else if (parentOp.isa(ScfOps.WhileOp.class)) {
        ScfOps.WhileOp whileOp = parentOp.as(ScfOps.WhileOp.class).orElseThrow();
        return handleWhileOp(continueOp, whileOp, state);
      }
      // Only loops need special handling. IfOp, ScopeOps or the like simply mark their end using
      // the
      // ContinueOp
      else {
        return Action.Terminate(null);
      }
    }

    public Action handleForOp(ScfOps.ForOp forOp, State state) {
      Quartet<Long, Long, Long, Long> bounds = ForRunner.getBounds(forOp, state);
      long lowerBoundNum = bounds.value1();
      long upperBoundNum = bounds.value2();
      long stepNum = bounds.value3();

      // Increment the body value (induction variable) by the step and check if we should continue
      // the
      // loop.
      long inductionValue = state.getValue(forOp.getInductionValue(), Long.class).orElseThrow();

      // Increment the induction value by the step.
      inductionValue += stepNum;
      state.setValue(forOp.getInductionValue(), inductionValue);

      // Check if we reached the end of the loop.
      if (inductionValue < upperBoundNum && inductionValue >= lowerBoundNum) {
        // Continue the loop by jumping to the beginning of the loop body region with the updated
        // induction variable.
        return Action.JumpToRegion(forOp.getRegion(), inductionValue);
      }

      // Terminate the loop.
      return Action.Terminate(null);
    }

    public Action handleWhileOp(ScfOps.ContinueOp continueOp, ScfOps.WhileOp whileOp, State state) {
      // Check if we are in the condition region
      if (whileOp.getConditionRegion().equals(continueOp.getParentRegion().orElseThrow())) {
        // If we are we need to jump to the body region to execute another iteration of the loop.
        return Action.JumpToRegion(whileOp.getBodyRegion());
      } else {
        // If we are not in the condition region we need to jump to the condition region to check if
        // we should continue the loop.
        return Action.JumpToRegion(whileOp.getConditionRegion());
      }
    }
  }

  final class ForRunner extends OpRunner implements ScfRunners {

    public ForRunner() {
      super(ScfOps.ForOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      ScfOps.ForOp forOp = op.as(ScfOps.ForOp.class).orElseThrow();
      Quartet<Long, Long, Long, Long> bounds = getBounds(forOp, state);
      long initialValueNum = bounds.value0();
      long lowerBoundNum = bounds.value1();
      long upperBoundNum = bounds.value2();

      // Step into the loop body if the initial value is less than the upper bound and greater than
      // or equal to the lower
      // bound, otherwise skip the loop.
      if (initialValueNum < upperBoundNum && initialValueNum >= lowerBoundNum) {
        // Set the body value (induction variable) to the initial value.
        return Action.StepIntoRegion(forOp.getRegion(), false, initialValueNum);
      } else {
        return Action.Next();
      }
    }

    public static Quartet<Long, Long, Long, Long> getBounds(ScfOps.ForOp forOp, State state) {
      Value initialValue = forOp.getInitialValue();
      Value lowerBound = forOp.getLowerBound();
      Value upperBound = forOp.getUpperBound();
      Value step = forOp.getStep();

      long initialValueNum = state.getValue(initialValue, Number.class).orElseThrow().longValue();
      long lowerBoundNum = state.getValue(lowerBound, Number.class).orElseThrow().longValue();
      long upperBoundNum = state.getValue(upperBound, Number.class).orElseThrow().longValue();
      long stepNum = state.getValue(step, Number.class).orElseThrow().longValue();

      return new Quartet<>(initialValueNum, lowerBoundNum, upperBoundNum, stepNum);
    }
  }

  final class IfRunner extends OpRunner implements ScfRunners {
    public IfRunner() {
      super(ScfOps.IfOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      ScfOps.IfOp ifOp = op.as(ScfOps.IfOp.class).orElseThrow();
      byte condition = state.getValue(ifOp.getOperand(0).orElseThrow(), Byte.class).orElseThrow();
      // Step into the then region if the condition is true, otherwise step into the else region if
      // it exists, or just
      // continue to the next operation.
      if (condition != 0) {
        return Action.StepIntoRegion(ifOp.getThenRegion(), false);
      } else if (ifOp.getElseRegion().isPresent()) {
        return Action.StepIntoRegion(ifOp.getElseRegion().get(), false);
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
      // Just step into the scope region.
      return Action.StepIntoRegion(op.getRegion(0).orElseThrow(), false);
    }
  }

  public class WhileRunner extends OpRunner {
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

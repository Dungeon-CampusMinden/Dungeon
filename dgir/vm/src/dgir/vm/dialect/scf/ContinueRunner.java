package dgir.vm.dialect.scf;

import static dialect.scf.ScfOps.*;
import static dialect.scf.ScfOps.ContinueOp;
import static dialect.scf.ScfOps.ForOp;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import io.arxila.javatuples.Quartet;
import org.jetbrains.annotations.NotNull;

public class ContinueRunner extends OpRunner {
  public ContinueRunner() {
    super(ContinueOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    ContinueOp continueOp = op.as(ContinueOp.class).orElseThrow();
    Operation parentOp = continueOp.getParentOperation().orElseThrow();
    // If we have a for op we need to handle it properly by incrementing the induction variable and
    // checking if we should continue the loop or not.
    if (parentOp.isa(ForOp.class)) {
      ForOp forOp = parentOp.as(ForOp.class).orElseThrow();
      return handleForOp(forOp, state);
    } else if (parentOp.isa(WhileOp.class)) {
      WhileOp whileOp = parentOp.as(WhileOp.class).orElseThrow();
      return handleWhileOp(continueOp, whileOp, state);
    }
    // Only loops need special handling. IfOp, ScopeOps or the like simply mark their end using the
    // ContinueOp
    else {
      return Action.Terminate(null);
    }
  }

  public Action handleForOp(ForOp forOp, State state) {
    Quartet<Long, Long, Long, Long> bounds = ForRunner.getBounds(forOp, state);
    long lowerBoundNum = bounds.value1();
    long upperBoundNum = bounds.value2();
    long stepNum = bounds.value3();

    // Increment the body value (induction variable) by the step and check if we should continue the
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

  public Action handleWhileOp(ContinueOp continueOp, WhileOp whileOp, State state) {
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

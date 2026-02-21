package dgir.vm.dialect.scf;

import core.ir.Operation;
import core.ir.Value;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.scf.ForOp;
import io.arxila.javatuples.Quartet;
import org.jetbrains.annotations.NotNull;

public class ForRunner extends OpRunner {

  public ForRunner() {
    super(ForOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    ForOp forOp = op.as(ForOp.class).orElseThrow();
    Quartet<Long, Long, Long, Long> bounds = getBounds(forOp, state);
    long initialValueNum = bounds.value0();
    long lowerBoundNum = bounds.value1();
    long upperBoundNum = bounds.value2();

    // Step into the loop body if the initial value is less than the upper bound and greater than or equal to the lower
    // bound, otherwise skip the loop.
    if (initialValueNum < upperBoundNum && initialValueNum >= lowerBoundNum) {
      // Set the body value (induction variable) to the initial value.
      return Action.StepInto(forOp.getRegion(), false, initialValueNum);
    } else {
      return Action.Next();
    }
  }

  public static Quartet<Long, Long, Long, Long> getBounds(ForOp forOp, State state) {
    Value initialValue = forOp.getOperandValue(0).orElseThrow();
    Value lowerBound = forOp.getOperandValue(1).orElseThrow();
    Value upperBound = forOp.getOperandValue(2).orElseThrow();
    Value step = forOp.getOperandValue(3).orElseThrow();

    long initialValueNum = state.getValue(initialValue, Number.class).orElseThrow().longValue();
    long lowerBoundNum = state.getValue(lowerBound, Number.class).orElseThrow().longValue();
    long upperBoundNum = state.getValue(upperBound, Number.class).orElseThrow().longValue();
    long stepNum = state.getValue(step, Number.class).orElseThrow().longValue();

    return new Quartet<>(initialValueNum, lowerBoundNum, upperBoundNum, stepNum);
  }
}

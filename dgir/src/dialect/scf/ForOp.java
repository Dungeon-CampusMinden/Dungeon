package dialect.scf;

import core.ir.*;
import core.traits.IControlFlow;
import core.traits.ISingleRegion;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Op which represents a for loop. It has one region for the body of the loop. Its parameters are: -
 * initValue: the initial value of the induction variable - lowerBound: the lower bound of the loop
 * - upperBound: the upper bound of the loop - step: the step size of the loop
 */
public final class ForOp extends ScfOp implements SCF, IControlFlow, ISingleRegion {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "scf.for";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public ForOp() {}

  public ForOp(Operation operation) {
    super(operation);
  }

  public ForOp(Value initValue, Value lowerBound, Value upperBound, Value step) {
    setOperation(
        true,
        Operation.Create(
            this,
            List.of(initValue, lowerBound, upperBound, step),
            null,
            null,
            List.of(IntegerT.INT32)));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  public Value getInductionValue() {
    return getRegion().getBodyValue(0).orElseThrow();
  }

  @Contract(pure = true)
  public Value getLowerBound() {
    return getRegion().getBodyValue(1).orElseThrow();
  }

  @Contract(pure = true)
  public Value getUpperBound() {
    return getRegion().getBodyValue(2).orElseThrow();
  }

  @Contract(pure = true)
  public Value getStep() {
    return getRegion().getBodyValue(3).orElseThrow();
  }
}

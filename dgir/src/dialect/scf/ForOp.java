package dialect.scf;

import core.ir.*;
import core.ir.Location;
import core.traits.IControlFlow;
import core.traits.ISingleRegion;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Counted for-loop in the {@code scf} dialect.
 *
 * <p>The loop body occupies a single region. The region receives four body values that are
 * accessible inside the body as the loop parameters:
 *
 * <ol>
 *   <li>the induction variable (current iteration value, {@code int32})
 *   <li>the lower bound (inclusive)
 *   <li>the upper bound (exclusive)
 *   <li>the step size
 * </ol>
 *
 * <p>The four operands passed at construction time seed the initial induction variable, lower
 * bound, upper bound, and step respectively.
 *
 * <p>Ident: {@code scf.for}
 *
 * <pre>{@code
 * scf.for (%i = %init to %upper step %step) {
 *   ...
 * }
 * }</pre>
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

  private ForOp() {}

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public ForOp(Operation operation) {
    super(operation);
  }

  /**
   * Create a for-loop with the given loop parameters.
   *
   * @param location   the source location of this operation.
   * @param initValue  the initial value of the induction variable.
   * @param lowerBound the lower bound of the loop (inclusive).
   * @param upperBound the upper bound of the loop (exclusive).
   * @param step       the step size per iteration.
   */
  public ForOp(@NotNull Location location, Value initValue, Value lowerBound, Value upperBound, Value step) {
    setOperation(
        true,
        Operation.Create(
            location,
            this,
            List.of(initValue, lowerBound, upperBound, step),
            null,
            null,
            List.of(IntegerT.INT32)));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the induction variable for the loop body.
   *
   * @return the body value at index 0.
   */
  @Contract(pure = true)
  public Value getInductionValue() {
    return getRegion().getBodyValue(0).orElseThrow();
  }

  /**
   * Returns the lower bound value (operand 1) visible inside the loop body.
   *
   * @return the body value at index 1.
   */
  @Contract(pure = true)
  public Value getLowerBound() {
    return getRegion().getBodyValue(1).orElseThrow();
  }

  /**
   * Returns the upper bound value (operand 2) visible inside the loop body.
   *
   * @return the body value at index 2.
   */
  @Contract(pure = true)
  public Value getUpperBound() {
    return getRegion().getBodyValue(2).orElseThrow();
  }

  /**
   * Returns the step value (operand 3) visible inside the loop body.
   *
   * @return the body value at index 3.
   */
  @Contract(pure = true)
  public Value getStep() {
    return getRegion().getBodyValue(3).orElseThrow();
  }
}

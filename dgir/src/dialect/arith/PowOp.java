package dialect.arith;

import core.ir.Location;
import core.ir.Operation;
import core.ir.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Raises the left-hand operand to the power of the right-hand operand. */
public final class PowOp extends BinaryNumericResultOp {

  @Override
  public @NotNull String getIdent() {
    return "arith.pow";
  }

  private PowOp() {}

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public PowOp(@NotNull Operation operation) {
    super(operation);
  }

  /**
   * Create a pow op with two numeric operands.
   *
   * @param loc the source location of this operation.
   * @param lhs the base operand.
   * @param rhs the exponent operand.
   */
  public PowOp(@NotNull Location loc, @NotNull Value lhs, @NotNull Value rhs) {
    setOperation(
        Operation.Create(
            loc, this, List.of(lhs, rhs), null, getDominantType(lhs.getType(), rhs.getType())));
  }
}

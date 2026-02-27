package dialect.arith;

import core.ir.Location;
import core.ir.Operation;
import core.ir.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Computes the maximum of two numeric operands.
 */
public final class MaxOp extends BinaryNumericResultOp {

  @Override
  public @NotNull String getIdent() {
    return "arith.max";
  }

  private MaxOp() {}

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public MaxOp(@NotNull Operation operation) {
    super(operation);
  }

  /**
   * Create a max op with two numeric operands.
   *
   * @param loc the source location of this operation.
   * @param lhs the left-hand operand.
   * @param rhs the right-hand operand.
   */
  public MaxOp(@NotNull Location loc, @NotNull Value lhs, @NotNull Value rhs) {
    setOperation(Operation.Create(loc, this, List.of(lhs, rhs), null, getDominantType(lhs.getType(), rhs.getType())));
  }
}

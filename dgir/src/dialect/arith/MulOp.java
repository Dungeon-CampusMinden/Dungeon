package dialect.arith;

import core.ir.Location;
import core.ir.Operation;
import core.ir.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Multiplies two numeric operands.
 */
public final class MulOp extends BinaryNumericResultOp implements Arith {

  @Override
  public @NotNull String getIdent() {
    return "arith.mul";
  }

  private MulOp() {}

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public MulOp(@NotNull Operation operation) {
    super(operation);
  }

  /**
   * Create a mul op with two numeric operands.
   *
   * @param loc the source location of this operation.
   * @param lhs the left-hand operand.
   * @param rhs the right-hand operand.
   */
  public MulOp(@NotNull Location loc, @NotNull Value lhs, @NotNull Value rhs) {
    setOperation(Operation.Create(loc, this, List.of(lhs, rhs), null, getDominantType(lhs.getType(), rhs.getType())));
  }
}

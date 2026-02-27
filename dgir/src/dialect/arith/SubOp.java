package dialect.arith;

import core.ir.Location;
import core.ir.Operation;
import core.ir.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Subtracts two numeric operands. */
public final class SubOp extends BinaryNumericResultOp implements Arith {

  @Override
  public @NotNull String getIdent() {
    return "arith.sub";
  }

  private SubOp() {}

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public SubOp(@NotNull Operation operation) {
    super(operation);
  }

  /**
   * Create a sub op with two numeric operands.
   *
   * @param loc the source location of this operation.
   * @param lhs the left-hand operand.
   * @param rhs the right-hand operand.
   */
  public SubOp(@NotNull Location loc, @NotNull Value lhs, @NotNull Value rhs) {
    setOperation(
        Operation.Create(
            loc, this, List.of(lhs, rhs), null, getDominantType(lhs.getType(), rhs.getType())));
  }
}

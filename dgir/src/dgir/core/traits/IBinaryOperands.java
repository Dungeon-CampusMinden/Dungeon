package dgir.core.traits;

import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.core.ir.ValueOperand;
import org.jetbrains.annotations.NotNull;

/**
 * Constrains an operation to have exactly two value operands.
 *
 * <p>Convenience accessors {@link #getLhs()} and {@link #getRhs()} delegate to the first and second
 * operand slots, respectively.
 */
public interface IBinaryOperands extends IOpTrait {
  default boolean verify(IBinaryOperands ignored) {
    Operation op = getOperation();
    if (op.getOperands().size() != 2) {
      op.emitError("Operation must have exactly two operands.");
      return false;
    }
    if (op.getOperandType(0).isEmpty() || op.getOperandType(1).isEmpty()) {
      op.emitError("Operation must have non-null operands");
      return false;
    }
    return true;
  }

  default @NotNull Value getLhs() {
    return getOperation()
        .getOperand(0)
        .flatMap(ValueOperand::getValue)
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Expected first operand to be a value for binary operation: "
                        + getOperation()));
  }

  default @NotNull Value getRhs() {
    return getOperation()
        .getOperand(1)
        .flatMap(ValueOperand::getValue)
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Expected second operand to be a value for binary operation: "
                        + getOperation()));
  }
}

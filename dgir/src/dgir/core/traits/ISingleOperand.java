package dgir.core.traits;

import dgir.core.ir.Type;
import dgir.core.ir.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Constrains an operation to have exactly one value operand.
 *
 * <p>Convenience accessors {@link #getOperand()} and {@link #getOperandType()} delegate to the
 * first operand slot.
 */
public interface ISingleOperand extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull ISingleOperand ignored) {
    // Ensure that the operation only has one operator
    if (getOperation().getOperands().size() != 1) {
      getOperation().emitError("Operation must have exactly one operand.");
      return false;
    }
    if (getOperation().getOperand(0).isEmpty()) {
      getOperation().emitError("Operation must have non-null operand");
      return false;
    }
    return true;
  }

  @Contract(pure = true)
  default @NotNull Value getOperand() {
    return getOperation().getOperandValue(0).orElseThrow();
  }

  @Contract(pure = true)
  default @NotNull Type getOperandType() {
    return getOperand().getType();
  }
}

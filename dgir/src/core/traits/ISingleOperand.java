package core.traits;

import core.ir.Type;
import core.ir.Value;
import core.ir.ValueOperand;
import java.util.Optional;
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
    return true;
  }

  @Contract(pure = true)
  default @NotNull Optional<Value> getOperand() {
    return getOperation().getOperand(0).flatMap(ValueOperand::getValue);
  }

  @Contract(pure = true)
  default @NotNull Optional<Type> getOperandType() {
    return getOperand().map(Value::getType);
  }
}

package core.traits;

import core.ir.Type;
import core.ir.Value;
import core.ir.ValueOperand;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A trait for operations that can have zero or one operand. This is used for operations like
 * "return" that can optionally return a value.
 */
public interface IZeroOrOneOperand extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull IZeroOrOneOperand ignored) {
    // Ensure that the operation only has one operator
    if (get().getOperands().size() > 1) {
      get().emitError("Operation must have at most one operand.");
      return false;
    }
    return true;
  }

  /**
   * Gets the operand of the operation, if it exists. If the operation has no operands, returns an
   * empty Optional.
   *
   * @return The operand of the operation, if it exists.
   */
  @SuppressWarnings("OptionalMapToOptional")
  @Contract(pure = true)
  default @NotNull Optional<Optional<Value>> getOperand() {
    if (get().getOperands().isEmpty()) return Optional.empty();
    return Optional.of(get().getOperand(0).flatMap(ValueOperand::getValue));
  }

  /**
   * Gets the type of the operand, if it exists. If the operation has no operands, returns an empty
   * Optional. If the operation has an operand, but the operand does not have a type, returns an
   * Optional containing an empty Optional.
   *
   * @return The type of the operand, if it exists.
   */
  @SuppressWarnings("OptionalMapToOptional")
  @Contract(pure = true)
  default @NotNull Optional<Optional<@NotNull Type>> getOperandType() {
    return getOperand().map(value -> value.map(Value::getType));
  }
}

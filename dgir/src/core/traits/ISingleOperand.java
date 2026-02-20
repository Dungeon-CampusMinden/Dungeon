package core.traits;

import core.ir.Type;
import core.ir.Value;
import core.ir.ValueOperand;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ISingleOperand extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull ISingleOperand op) {
    // Ensure that the operation only has one operator
    if (get().getOperands().size() == 1) {
      get().emitError("Operation must have exactly one operand.");
      return false;
    }
    return true;
  }

  @Contract(pure = true)
  default @NotNull Optional<Value> getOperand() {
    return get().getOperand(0).flatMap(ValueOperand::getValue);
  }

  @Contract(pure = true)
  default @NotNull Optional<Type> getOperandType() {
    return getOperand().map(Value::getType);
  }
}

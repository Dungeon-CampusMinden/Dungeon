package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.ir.Type;
import blockly.vm.dgir.core.ir.Value;

import java.util.Optional;

/**
 * A trait for operations that can have zero or one operand. This is used for operations like "return" that can optionally
 * return a value.
 */
public interface IZeroOrOneOperand extends IOpTrait {
  default boolean verify(IZeroOrOneOperand op) {
    // Ensure that the operation only has one operator
    if (get().getOperands().size() > 1) {
      get().emitError("Operation must have at most one operand.");
      return false;
    }
    return true;
  }

  default Optional<Value> getOperand() {
    if (get().getOperands().isEmpty()) return Optional.empty();
    return Optional.ofNullable(get().getOperands().getFirst().getValue());
  }

  default Optional<Type> getOperandType() {
    return getOperand().map(Value::getType);
  }
}

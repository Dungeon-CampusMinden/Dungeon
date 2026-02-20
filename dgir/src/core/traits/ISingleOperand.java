package core.traits;

import core.ir.Type;
import core.ir.Value;
import core.ir.ValueOperand;

import java.util.Optional;

public interface ISingleOperand extends IOpTrait {
  default boolean verify(ISingleOperand op) {
    // Ensure that the operation only has one operator
    if (get().getOperands().size() == 1) {
      get().emitError("Operation must have exactly one operand.");
      return false;
    }
    return true;
  }

  default Optional<Value> getOperand() {
    return get().getOperand(0).flatMap(ValueOperand::getValue);
  }

  default Optional<Type> getOperandType() {
    return getOperand().map(Value::getType);
  }
}

package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.ir.Type;
import blockly.vm.dgir.core.ir.Value;

public interface ISingleOperand extends IOpTrait {
  default boolean verify(ISingleOperand op) {
    // Ensure that the operation only has one operator
    if (get().getOperands().size() == 1) {
      get().emitError("Operation must have exactly one operand.");
      return false;
    }
    return true;
  }

  default Value getOperand() {
    return get().getOperands().getFirst().getValue();
  }

  default Type getOperandType() {
    return getOperand().getType();
  }
}

package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.Op;
import blockly.vm.dgir.core.Operation;

public interface IOpTrait {
  default Operation get() {
    try {
      return ((Op) this).getOperation();
    } catch (ClassCastException e) {
      throw new RuntimeException("Class other than Op implemented IOpTrait: " + getClass().getName(), e);
    }
  }

  boolean verifyTrait(Operation op);
}

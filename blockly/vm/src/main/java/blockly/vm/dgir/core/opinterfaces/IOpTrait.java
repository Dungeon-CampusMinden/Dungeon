package blockly.vm.dgir.core.opinterfaces;

import blockly.vm.dgir.core.Op;
import blockly.vm.dgir.core.Operation;

public interface IOpTrait<
  // Deriving types must both be an Op and implement this interface so that we can be sure this is the derived type
  // and access the get operation function of the derived T
  // This is ugly but the only way to access said function.
  DerivedT extends Op & IOpTrait<DerivedT>> {
  default Operation getOperation() {
    return ((DerivedT) this).getOperation();
  }

  boolean verifyTrait(Operation op);
}

package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.Block;
import blockly.vm.dgir.core.Op;

/**
 * This interface marks an operation as having an input on the control flow of the program.
 * Ops that should use this interface include cf.branch, cf.branch_if, etc.
 */
public interface IControlFlowOp extends IOpTrait {
  default Block getSuccessor(){
    assert get().getBlockOperands().size() == 1 : "Control flow ops must have exactly one block operand/successor.";
    return get().getBlockOperands().getFirst().getValue();
  }
}

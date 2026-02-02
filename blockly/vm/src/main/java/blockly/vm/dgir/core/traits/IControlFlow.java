package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.Block;
import blockly.vm.dgir.core.Operation;

/**
 * This interface marks an operation as having an input on the control flow of the program.
 * Ops that should use this interface include cf.branch, cf.branch_if, etc.
 */
public interface IControlFlow extends IOpTrait {
  static Block getSuccessor(Operation op){
    assert op.hasTrait(IControlFlow.class) : "Operation does not implement IControlFlow.";
    assert op.getBlockOperands().size() == 1 : "Control flow ops must have exactly one block operand/successor.";
    return op.getBlockOperands().getFirst().getValue();
  }

  static boolean isBranch(Operation op){
    assert op.hasTrait(IControlFlow.class) : "Operation does not implement IControlFlow.";
    return op.getRegions().isEmpty();
  }

  static boolean isStructured(Operation op){
    assert op.hasTrait(IControlFlow.class) : "Operation does not implement IControlFlow.";
    return !op.getRegions().isEmpty();
  }
}

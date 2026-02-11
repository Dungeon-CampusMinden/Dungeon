package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.Operation;

/**
 * Mark an operation as not having a terminator.
 */
public interface INoTerminator extends IOpTrait {
  default boolean verify(INoTerminator op) {
    Operation operation = get();
    // Check that the operation has exactly one region, block and no terminator.
    if (operation.getRegions().size() != 1){
      operation.emitError("Operation must have exactly one region.");
      return false;
    }
    if (operation.getRegions().getFirst().getBlocks().size() != 1){
      operation.emitError("Operation region must have exactly one block.");
      return false;
    }
    if (operation.getRegions().getFirst().getBlocks().getFirst().hasTerminator()){
      operation.emitError("Operation block must not have a terminator.");
      return false;
    }
    return true;
  }
}

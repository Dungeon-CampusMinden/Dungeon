package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.ir.*;

public interface ISingleRegion extends IOpTrait {
  default boolean verify(ISingleRegion trait) {
    if (get().getRegions().size() != 1) {
      get().emitError("Operation must have exactly one region.");
      return false;
    }
    return true;
  }

  default Region getRegion() {
    return get().getFirstRegion();
  }

  default Value getArgument(int index) {
    return getRegion().getBodyValue(index);
  }

  default Operation addOperation(Operation operation, int blockIndex) {
    return getRegion().getBlocks().get(blockIndex).addOperation(operation);
  }

  default <OpT extends Op> OpT addOperation(OpT op, int blockIndex){
    return getRegion().getBlocks().get(blockIndex).addOperation(op);
  }

  default Block getEntryBlock() {
    return getRegion().getEntryBlock();
  }

  default Block getBlock(int index) {
    return getRegion().getBlocks().get(index);
  }

  default Block addBlock(Block block){
    getRegion().addBlock(block);
    return block;
  }
}

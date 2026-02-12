package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.ir.Block;
import blockly.vm.dgir.core.ir.Op;
import blockly.vm.dgir.core.ir.Operation;
import blockly.vm.dgir.core.ir.Region;

public interface ISingleBlock extends IOpTrait {
  default boolean verify(ISingleBlock op) {
    if (get().getRegions().size() != 1) {
      get().emitError("Operation must have exactly one region.");
      return false;
    }
    if (get().getFirstRegion().getBlocks().size() != 1) {
      get().emitError("Operation's single region must have exactly one block.");
      return false;
    }
    return true;
  }

  default Block getBlock() {
    return get().getFirstRegion().getEntryBlock();
  }

  default Operation addOperation(Operation operation) {
    get().getFirstRegion().getEntryBlock().addOperation(operation);
    return operation;
  }

  default <OpT extends Op> OpT addOperation(OpT op){
    get().getFirstRegion().getEntryBlock().addOperation(op);
    return op;
  }
}

package core.traits;

import core.ir.Block;
import core.ir.Op;
import core.ir.Operation;
import core.ir.Region;

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

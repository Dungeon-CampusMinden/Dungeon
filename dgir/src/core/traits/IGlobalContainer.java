package core.traits;

import core.ir.Block;
import core.ir.Operation;
import core.ir.Region;

/**
 * Marks an operation that can only contain global operations. This is used to mark the top-level container of a module,
 * which can only contain global operations.
 */
public interface IGlobalContainer extends IOpTrait {
  default boolean verify(IGlobalContainer op) {
    // Ensure that all operations contained in the regions are global operations.
    for (Region region : op.get().getRegions()) {
      for (Block block : region.getBlocks()) {
        for (Operation operation : block.getOperations()) {
          if (!operation.hasTrait(IGlobal.class)) {
            operation.emitError("Operation " + operation.getDetails().getIdent() + " is not a global operation and cannot be contained in a global container.");
            return false;
          }
        }
      }
    }
    return true;
  }
}

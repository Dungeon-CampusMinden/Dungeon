package dgir.core.traits;

import dgir.core.ir.Block;
import dgir.core.ir.Operation;
import dgir.core.ir.Region;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Marks an operation that can only contain global operations. This is used to mark the top-level
 * container of a module, which can only contain global operations.
 */
public interface IGlobalContainer extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull IGlobalContainer op) {
    // Ensure that all operations contained in the regions are global operations.
    for (Region region : op.getOperation().getRegions()) {
      for (Block block : region.getBlocks()) {
        for (Operation operation : block.getOperations()) {
          if (!operation.hasTrait(IGlobal.class)) {
            operation.emitError(
                "Operation is not a global operation and cannot be contained in a global container.");
            return false;
          }
        }
      }
    }
    return true;
  }
}

package dgir.core.traits;

import dgir.core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/** Mark an operation as not having a terminator. */
public interface INoTerminator extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull INoTerminator ignored) {
    Operation operation = getOperation();
    // Check that the operation has exactly one region, block and no terminator.
    if (operation.getRegions().size() != 1) {
      operation.emitError("Operation must have exactly one region.");
      return false;
    }
    if (operation.getRegions().getFirst().getBlocks().size() != 1) {
      operation.emitError("Operation region must have exactly one block.");
      return false;
    }
    if (operation.getRegions().getFirst().getBlocks().getFirst().hasTerminator()) {
      operation.emitError("Operation block must not have a terminator.");
      return false;
    }
    return true;
  }
}

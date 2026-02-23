package core.traits;

import core.ir.Block;
import core.ir.Op;
import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ISingleBlock extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull ISingleBlock ignored) {
    if (getOperation().getRegions().size() != 1) {
      getOperation().emitError("Operation must have exactly one region.");
      return false;
    }
    if (getOperation().getFirstRegion().orElseThrow().getBlocks().size() != 1) {
      getOperation().emitError("Operation's single region must have exactly one block.");
      return false;
    }
    return true;
  }

  @Contract(pure = true)
  default @NotNull Block getBlock() {
    return getOperation().getFirstRegion().orElseThrow().getEntryBlock();
  }

  default @NotNull Operation addOperation(@NotNull Operation operation) {
    getOperation().getFirstRegion().orElseThrow().getEntryBlock().addOperation(operation);
    return operation;
  }

  default <OpT extends Op> @NotNull OpT addOperation(@NotNull OpT op) {
    getOperation().getFirstRegion().orElseThrow().getEntryBlock().addOperation(op);
    return op;
  }
}

package core.traits;

import core.ir.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ISingleRegion extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull ISingleRegion ignored) {
    if (getOperation().getRegions().size() != 1) {
      getOperation().emitError("Operation must have exactly one region.");
      return false;
    }
    return true;
  }

  @Contract(pure = true)
  default @NotNull Region getRegion() {
    return getOperation()
        .getFirstRegion()
        .orElseThrow(() -> new RuntimeException("Operation must have exactly one region."));
  }

  @Contract(pure = true)
  default @NotNull Optional<Value> getArgument(int index) {
    return getRegion().getBodyValue(index);
  }

  default @NotNull Operation addOperation(@NotNull Operation operation, int blockIndex) {
    return getRegion().getBlocks().get(blockIndex).addOperation(operation);
  }

  default <OpT extends Op> @NotNull OpT addOperation(@NotNull OpT op, int blockIndex) {
    return getRegion().getBlocks().get(blockIndex).addOperation(op);
  }

  @Contract(pure = true)
  default @NotNull Block getEntryBlock() {
    return getRegion().getEntryBlock();
  }

  @Contract(pure = true)
  default @NotNull Optional<Block> getBlock(int index) {
    if (index >= getRegion().getBlocks().size()) return Optional.empty();
    return Optional.ofNullable(getRegion().getBlocks().get(index));
  }

  default @NotNull Block addBlock(@NotNull Block block) {
    getRegion().addBlock(block);
    return block;
  }
}

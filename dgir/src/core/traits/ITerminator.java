package core.traits;

import core.ir.Block;
import core.ir.Operation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ITerminator extends IOpTrait {
  default boolean verify(@NotNull ITerminator trait) {
    // Make sure the terminator is the last operation in the region.
    Optional<Block> block = get().getParent();
    Operation self = get();
    if (block.isEmpty()) {
      self.emitError("Terminator must be in a block.");
      return false;
    }
    if (!block.get().getOperations().getLast().equals(self))
    {
      self.emitError("Terminator must be the last operation in the region.");
      return false;
    }
    return true;
  }
}

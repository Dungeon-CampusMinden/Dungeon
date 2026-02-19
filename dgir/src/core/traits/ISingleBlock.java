package core.traits;

import core.ir.Block;
import core.ir.Op;
import core.ir.Operation;
import core.ir.Region;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ISingleBlock extends IOpTrait {
  default boolean verify(ISingleBlock op) {
    if (get().getRegions().size() != 1) {
      get().emitError("Operation must have exactly one region.");
      return false;
    }
    if (get().getFirstRegion().orElseThrow().getBlocks().size() != 1) {
      get().emitError("Operation's single region must have exactly one block.");
      return false;
    }
    return true;
  }

  default @NotNull Block getBlock() {
    return get().getFirstRegion().orElseThrow().getEntryBlock();
  }

  default @NotNull Operation addOperation(@NotNull Operation operation) {
    get().getFirstRegion().orElseThrow().getEntryBlock().addOperation(operation);
    return operation;
  }

  default <OpT extends Op> @NotNull OpT addOperation(@NotNull OpT op){
    get().getFirstRegion().orElseThrow().getEntryBlock().addOperation(op);
    return op;
  }
}

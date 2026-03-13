package dgir.core.traits;

import dgir.core.OperationVerifier;
import dgir.core.ir.Block;
import dgir.core.ir.Operation;
import dgir.core.ir.Region;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Optional;

/**
 * Marks an operation as a block terminator — the last operation in a {@link Block} that transfers
 * control to a successor or returns.
 *
 * <p>The verifier checks that the op is placed inside a block and that it is indeed the last
 * operation in that block. {@link OperationVerifier} also checks this structurally during
 * block-exit validation.
 */
public interface ITerminator extends IOpTrait {
  default boolean verify(@NotNull ITerminator ignored) {
    // Make sure the terminator is the last operation in the region.
    Optional<Block> block = getOperation().getParent();
    Operation self = getOperation();
    if (block.isEmpty()) {
      self.emitError("Terminator must be in a block.");
      return false;
    }
    if (!block.get().getOperations().getLast().equals(self)) {
      self.emitError(
          "Terminator must be the last operation in the block.\n\t"
              + " Found terminator at index "
              + block.get().getOperations().indexOf(self)
              + " but expected at index "
              + (block.get().getOperations().size() - 1)
              + " \n\tGot terminator: \n\t\t"
              + block.get().getOperations().getLast()
              + " \n\tin operation: \n\t\t"
              + self.getParentOperation().orElse(null)
              + " \n\tin region\n\t\t "
              + block.get().getParent().map(Region::getIndex).orElse(null));
      return false;
    }
    return true;
  }

  /**
   * Returns the constructor for this terminator which takes a location.
   *
   * @return the constructor for this terminator which takes a location.
   */
  @NotNull
  Optional<Constructor<? extends ITerminator>> getLocationConstructor();
}

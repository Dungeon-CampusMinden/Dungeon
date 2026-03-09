package dgir.core.traits;

import dgir.core.OperationVerifier;
import dgir.core.ir.Block;
import dgir.core.ir.Operation;
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
      self.emitError("Terminator must be the last operation in the region.");
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

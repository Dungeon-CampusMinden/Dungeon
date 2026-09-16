package rooms.systemRecovery.modules.computer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/** Ensures a mounted chip is either returned or consumed only once across repeated callbacks. */
final class ComputerChipSession {

  private final AtomicBoolean resolved = new AtomicBoolean();

  /**
   * Runs a chip transfer once. A failed transfer leaves the session open for a later retry.
   *
   * @param transfer operation that returns or consumes the mounted chip
   * @return whether this call completed the transfer
   */
  boolean resolve(BooleanSupplier transfer) {
    if (!resolved.compareAndSet(false, true)) return false;
    boolean completed = false;
    try {
      completed = transfer.getAsBoolean();
      return completed;
    } finally {
      if (!completed) resolved.set(false);
    }
  }

  /**
   * @return whether a previous callback already returned or consumed the chip
   */
  boolean resolved() {
    return resolved.get();
  }
}

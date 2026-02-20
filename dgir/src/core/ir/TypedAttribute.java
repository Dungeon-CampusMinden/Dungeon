package core.ir;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link Attribute} that carries an associated {@link Type}, allowing the stored
 * value to be type-checked at the IR level.
 */
public abstract class TypedAttribute extends Attribute {

  // =========================================================================
  // Members
  // =========================================================================

  private final @NotNull Type type;

  // =========================================================================
  // Constructors
  // =========================================================================

  protected TypedAttribute(@NotNull Type type) {
    this.type = type;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  public @NotNull Type getType() {
    return type;
  }
}

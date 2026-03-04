package dgir.core.ir;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link Attribute} that carries an associated {@link Type}, allowing the stored value to be
 * type-checked at the IR level.
 */
public abstract class TypedAttribute extends Attribute {

  // =========================================================================
  // Members
  // =========================================================================

  private final @NotNull Type type;

  // =========================================================================
  // Constructors
  // =========================================================================

  /**
   * Create a typed attribute associated with the given type.
   *
   * @param type the type that governs validation of the stored value.
   */
  protected TypedAttribute(@NotNull Type type) {
    this.type = type;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the type associated with this attribute.
   *
   * @return the type, never {@code null}.
   */
  @Contract(pure = true)
  public @NotNull Type getType() {
    return type;
  }
}

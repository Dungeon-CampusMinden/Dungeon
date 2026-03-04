package dgir.core.ir;

import com.fasterxml.jackson.annotation.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * The single result value produced by an {@link Operation}. Wraps a {@link Value} and enforces
 * type-consistency when the value is replaced.
 */
public class OperationResult {

  // =========================================================================
  // Members
  // =========================================================================

  @JsonIdentityReference @JsonValue private @NotNull Value value;

  @JsonIgnore private final @NotNull Operation parent;

  // =========================================================================
  // Constructors
  // =========================================================================

  /**
   * Create a result wrapping an existing {@link Value}.
   *
   * @param parent the operation that produces this result.
   * @param value  the pre-existing value to wrap.
   */
  public OperationResult(@NotNull Operation parent, @NotNull Value value) {
    this.parent = parent;
    this.value = value;
  }

  /**
   * Create a result by allocating a fresh {@link Value} of the given type.
   *
   * @param parent the operation that produces this result.
   * @param type   the type of the new value.
   */
  public OperationResult(@NotNull Operation parent, @NotNull Type type) {
    this.parent = parent;
    this.value = new Value(type);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the {@link Value} wrapped by this result.
   *
   * @return the result value, never {@code null}.
   */
  @Contract(pure = true)
  public @NotNull Value getValue() {
    return value;
  }

  /**
   * Replace the result value, enforcing that the type is unchanged.
   *
   * @param value the new result value; its type must match the current result type.
   * @throws AssertionError if the types do not match.
   */
  public void setValue(@NotNull Value value) {
    assert value.getType().equals(this.value.getType())
        : "Type mismatch while setting result value: "
            + value.getType()
            + " != "
            + this.value.getType();
    this.value = value;
  }

  /**
   * Returns the type of the result value.
   *
   * @return the result type, never {@code null}.
   */
  @Contract(pure = true)
  public @NotNull Type getType() {
    return value.getType();
  }

  /**
   * Returns the operation that owns this result.
   *
   * @return the parent operation, never {@code null}.
   */
  @Contract(pure = true)
  public @NotNull Operation getParent() {
    return parent;
  }
}

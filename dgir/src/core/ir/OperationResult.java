package core.ir;

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

  public OperationResult(@NotNull Operation parent, @NotNull Value value) {
    this.parent = parent;
    this.value = value;
  }

  public OperationResult(@NotNull Operation parent, @NotNull Type type) {
    this.parent = parent;
    this.value = new Value(type);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  public @NotNull Value getValue() {
    return value;
  }

  public void setValue(@NotNull Value value) {
    assert value.getType().equals(this.value.getType())
        : "Type mismatch while setting result value: "
            + value.getType()
            + " != "
            + this.value.getType();
    this.value = value;
  }

  @Contract(pure = true)
  public @NotNull Type getType() {
    return value.getType();
  }

  @Contract(pure = true)
  public @NotNull Operation getParent() {
    return parent;
  }
}

package core.ir;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import core.IRObjectWithUseList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A reference to a value used as an operand to an {@link Operation}. Manages its own entry in the
 * referenced value's use-list.
 *
 * @param <ValueT> The type of value being referenced (e.g. {@link Value} or {@link Block}).
 * @param <DerivedT> The concrete operand subclass (for self-referential use-list typing).
 */
@SuppressWarnings("unchecked")
public abstract class Operand<
    // The value type accepted by this operand
    ValueT extends IRObjectWithUseList<ValueT, DerivedT>,
    // The type extending from this class
    DerivedT extends Operand<ValueT, DerivedT>> {

  // =========================================================================
  // Members
  // =========================================================================

  /** The value referenced by this operand. */
  @JsonIdentityReference(alwaysAsId = true)
  private @Nullable ValueT value;

  /** The operation that owns this operand. */
  @JsonIgnore private final @NotNull Operation owner;

  // =========================================================================
  // Constructors
  // =========================================================================

  public Operand(@NotNull Operation owner) {
    this.owner = owner;
  }

  public Operand(@NotNull Operation owner, @Nullable ValueT value) {
    this.owner = owner;
    setValue(value);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Get the index of this operand in its owner's operand list.
   *
   * @return The index, or -1 if not found.
   */
  @Contract(pure = true)
  @JsonIgnore
  public abstract int getIndex();

  /**
   * Get the operation that owns this operand.
   *
   * @return The owning operation.
   */
  @Contract(pure = true)
  public @NotNull Operation getOwner() {
    return owner;
  }

  /**
   * Get the value referenced by this operand.
   *
   * @return The referenced value.
   */
  @Contract(pure = true)
  public @NotNull Optional<ValueT> getValue() {
    return Optional.ofNullable(value);
  }

  /**
   * Get the use-list object for the value currently referenced by this operand.
   *
   * @return The use-list of the current value.
   */
  @Contract(pure = true)
  public @NotNull Optional<IRObjectWithUseList<ValueT, DerivedT>> geCurrentUseList() {
    return Optional.ofNullable(value);
  }

  /**
   * Point this operand at a new value, updating both the old and new use-lists.
   *
   * @param value The new value to reference.
   */
  public void setValue(@Nullable ValueT value) {
    removeFromCurrentUseList();
    this.value = value;
    insertIntoCurrentUseList();
  }

  // =========================================================================
  // Use-list Management
  // =========================================================================

  /** Insert this operand into the use-list of the currently referenced value. */
  private void insertIntoCurrentUseList() {
    if (value != null) {
      //noinspection unchecked
      value.getUses().add((DerivedT) this);
    }
  }

  /** Remove this operand from the use-list of the currently referenced value. */
  private void removeFromCurrentUseList() {
    if (value != null)
      //noinspection unchecked
      value.getUses().remove((DerivedT) this);
  }

  /**
   * Drop this operand from the use-list and clear the reference. Should only be called by
   * subclasses during teardown.
   */
  protected void drop() {
    removeFromCurrentUseList();
    this.value = null;
  }
}

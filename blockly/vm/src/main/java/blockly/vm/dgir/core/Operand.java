package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * A reference to a value used as an operand to an operation.
 *
 * @param <ValueT> The type of value being referenced. Typically, a value but could also be a block or other type (branching operations)
 */
public abstract class Operand<
  // The value type accepted by this operand
  ValueT extends IRObjectWithUseList<ValueT, DerivedT>,
  // The type extending from this class
  DerivedT extends Operand<ValueT, DerivedT>
  > {

  /**
   * The value referenced by this operand
   */
  private ValueT value;

  /**
   * The operation that owns this operand
   */
  @JsonBackReference
  private final Operation owner;

  public Operand(Operation owner) {
    this.owner = owner;
  }

  public Operand(Operation owner, ValueT value) {
    this.owner = owner;
    setValue(value);
  }

  /**
   * Get the index number of this operand in the owner's operand list.
   *
   * @return The index number of this operand, or -1 if not found.
   */
  public int GetOperandNumber() {
    int index = -1;
    for (var operand : owner.getOperands()) {
      ++index;
      if (operand == this) return index;
    }
    return -1;
  }

  /**
   * Get the operation that owns this operand.
   *
   * @return The operation that owns this operand.
   */
  public Operation getOwner() {
    return owner;
  }

  /**
   * Get the value being used by this operand.
   *
   * @return The value being used by this operand.
   */
  public ValueT getValue() {
    return value;
  }

  /**
   * Get the use list for the current value being used by this operand.
   *
   * @return The use list for the current value being used by this operand.
   */
  public IRObjectWithUseList<ValueT, DerivedT> geCurrentUseList() {
    return value;
  }

  /**
   * Set the value being used by this operand.
   *
   * @param value The new value.
   */
  public void setValue(ValueT value) {
    removeFromCurrentUseList();
    this.value = value;
    insertIntoCurrentUseList();
  }

  /**
   * Insert this operand into the use list of the value currently stored
   */
  private void insertIntoCurrentUseList() {
    if (value != null)
      value.getUses().add((DerivedT) this);
  }

  private void removeFromCurrentUseList() {
    if (value != null)
      value.getUses().remove((DerivedT) this);
  }

  /**
   * Drop this operand from the use list and set its value to null
   */
  protected void drop() {
    removeFromCurrentUseList();
    this.value = null;
  }
}

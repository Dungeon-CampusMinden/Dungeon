package blockly.vm.dgir.core;


import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * A reference to a value used as an operand to an operation.
 *
 * @param <ValueT> The type of value being referenced. Typically, a value but could also be a block or other type (branching operations)
 */
public abstract class Operand<ValueT> {
  @JsonBackReference
  private Operation parent;

  /**
   * Get the value being used by this operand.
   *
   * @return The value being used by this operand.
   */
  public abstract ValueT getValue();

  /**
   * Set the value being used by this operand.
   *
   * @param value The new value.
   */
  public abstract void setValue(ValueT value);

  public int GetOperandNumber() {
    int index = -1;
    for (var operand : parent.getOperands()) {
      ++index;
      if (operand == this) return index;
    }
    return -1;
  }

  public Operation getParent() {
    return parent;
  }

  public void setParent(Operation parent) {
    assert Utils.Caller.getCallingClass() == Operation.class : "Assigning the parent of an operand is only allowed from the Operation class. Was called from " + Utils.Caller.getCallingClass().getName();
    assert this.parent == null || parent == null : "Operand already has a parent.";

    this.parent = parent;
  }
}

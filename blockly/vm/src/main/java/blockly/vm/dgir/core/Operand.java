package blockly.vm.dgir.core;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityReference;

/**
 * A reference to a value used as an operand to an operation.
 *
 * @param <ValueT> The type of value being referenced. Typically, a value but could also be a block or other type (branching operations)
 */
public abstract class Operand<ValueT extends Value> {
  @JsonBackReference
  public Operation owner;

  /**
   * Get the value being used by this operand.
   *
   * @return The value being used by this operand.
   */
  @JsonIdentityReference(alwaysAsId = true)
  abstract ValueT getValue();

  /**
   * Set the value being used by this operand.
   *
   * @param value The new value.
   */
  abstract void setValue(ValueT value);

  public int GetOperandNumber() {
    int index = -1;
    assert owner.getOperands() != null;
    for (var operand : owner.getOperands()) {
      ++index;
      if (operand == this) return index;
    }
    return -1;
  }
}

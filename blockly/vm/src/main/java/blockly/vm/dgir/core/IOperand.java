package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IOperand<ValueT extends Value> {
  /**
   * Get the operation that owns this operand.
   * @return The operation that owns this operand.
   */
  @JsonIgnore
  public Operation getOwner();

  /**
   * Get the value being used by this operand.
   *
   * @return The value being used by this operand.
   */
  @JsonIgnore
  public ValueT getValue();

  /**
   * Set the value being used by this operand.
   */
  public void setValue(ValueT value);

  default int GetOperandNumber(){
    return getOwner().getOperands().indexOf(this);
  }
}

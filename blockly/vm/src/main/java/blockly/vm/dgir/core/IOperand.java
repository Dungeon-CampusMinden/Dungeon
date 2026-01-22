package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IOperand<ValueT extends Value> extends IIdentifiableType {
  /**
   * Get the operation that owns this operand.
   * @return The operation that owns this operand.
   */
  @JsonIgnore
  Operation getOwner();

  /**
   * Set the operation that owns this operand.
   */
  void setOwner(Operation owner);

  /**
   * Get the value being used by this operand.
   *
   * @return The value being used by this operand.
   */
  @JsonIgnore
  ValueT getValue();

  /**
   * Set the value being used by this operand.
   */
  void setValue(ValueT value);

  default int GetOperandNumber(){
    return getOwner().getOrCreateOperands().indexOf(this);
  }
}

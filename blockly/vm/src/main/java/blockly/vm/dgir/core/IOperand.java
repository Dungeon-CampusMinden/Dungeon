package blockly.vm.dgir.core;


import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A reference to a value used as an operand to an operation.
 * @param <ValueT> The type of value being referenced. Typically, a value but could also be a block or other type (branching operations)
 */
public interface IOperand<ValueT extends Value> extends IIdentifiableType {
  /**
   * Get the operation that owns this operand.
   * @return The operation that owns this operand.
   */
  @JsonIgnore
  Operation getOwner();


  /**
   * Get the value being used by this operand.
   *
   * @return The value being used by this operand.
   */
  @JsonIdentityReference(alwaysAsId = true)
  ValueT getValue();

  /**
   * Set the value being used by this operand.
   */
  void setValue(ValueT value);

  default int GetOperandNumber(){
    return getOwner().getOrCreateOperands().indexOf(this);
  }
}

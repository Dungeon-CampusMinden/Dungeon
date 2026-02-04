package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A reference to a dynamic value, which could be supplied by an operation,
 * or other source such as block arguments.
 *
 */
public final class ValueOperand extends Operand<Value, ValueOperand> {
  public ValueOperand(Operation owner, Value value) {
    super(owner, value);
  }

  @JsonIgnore
  public Type getType() {
    assert getValue() != null : "ValueOperand has no value assigned.";
    return getValue().getType();
  }
}

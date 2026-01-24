package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

/**
 * A reference to a dynamic value, which could be supplied by an operation,
 * or other source such as block arguments.
 * */
public final class ValueOperand extends Operand<Value> {
  @JsonIdentityReference
  private Value value;

  public ValueOperand(Value value) {
    this.value = value;
  }

  @Override
  public Value getValue() {
    return value;
  }

  @Override
  public void setValue(Value value) {
    this.value = value;
  }
}

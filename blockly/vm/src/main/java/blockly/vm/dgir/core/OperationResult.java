package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

public class OperationResult {
  @JsonIdentityReference(alwaysAsId = false)
  @JsonValue
  private Value value;

  @JsonIgnore
  private final Operation parent;

  public OperationResult(Operation parent, Value value) {
    this.parent = parent;
    assert value != null : "Cannot set null value as result";
    this.value = value;
  }

  public OperationResult(Operation parent, Type type) {
    this.parent = parent;
    this.value = new Value(type);
  }

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    assert value != null : "Cannot set null value as result";
    assert value.getType().equals(this.value.getType()) : "Type mismatch while setting result value: " + value.getType() + " != " + this.value.getType();
    this.value = value;
  }

  public Type getType() {
    return value.getType();
  }

  public Operation getParent() {
    return parent;
  }
}

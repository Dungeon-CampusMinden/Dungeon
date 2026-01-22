package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class OperationResult extends Value {
  @JsonBackReference
  public Operation owner;

  public OperationResult(Type type) {
    super(type, Kind.OpResult);
  }

  public OperationResult(Value value) {
    super(value.getType(), Kind.OpResult);
  }
}

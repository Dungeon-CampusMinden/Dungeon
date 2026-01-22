package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

public class OperationResult extends Value {
  private Operation owner;

  public OperationResult() {
    super(null, Kind.OpResult);
    owner = null;
  }

  public OperationResult(Type type, Operation owner) {
    super(type, Kind.OpResult);
    this.owner = owner;
  }

  public OperationResult(Value value, Operation owner) {
    super(value.getType(), Kind.OpResult);
    this.owner = owner;
  }

  public OperationResult(OperationResult result, Operation owner) {
    super(result.getType(), Kind.OpResult);
    this.owner = owner;
  }

  @JsonIgnore
  public Operation getOwner() {
    return owner;
  }

  public void setOwner(Operation owner) {
    this.owner = owner;
  }
}

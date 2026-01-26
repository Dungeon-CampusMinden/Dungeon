package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;

public class OperationResult extends Value {
  @JsonBackReference
  private Operation parent;

  public OperationResult() {
    super(null, Kind.OpResult);
  }

  public OperationResult(Type type) {
    super(type, Kind.OpResult);
  }

  public OperationResult(Value value) {
    super(value.getType(), Kind.OpResult);
  }

  public Operation getParent() {
    return parent;
  }

  public void setParent(Operation parent) {
    assert Utils.Caller.getCallingClass() == Operation.class : "Assigning the owner of an operation result is only allowed from the Operation class. Was called from " + Utils.Caller.getCallingClass().getName();
    assert this.parent == null || this.parent == parent : "Operation result already assigned to another operation.";

    this.parent = parent;
  }
}

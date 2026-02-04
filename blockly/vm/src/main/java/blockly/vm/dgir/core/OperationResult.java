package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class OperationResult extends Value {
  @JsonIgnore
  private final Operation parent;

  public OperationResult(Operation parent, Type type) {
    super(type, Kind.OpResult);
    this.parent = parent;
  }

  public Operation getParent() {
    return parent;
  }
}

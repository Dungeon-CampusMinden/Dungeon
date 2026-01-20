package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public final class Block {
  private final String label;
  private final List<Operation> operations = new ArrayList<>();

  @JsonBackReference
  private final Region parent;

  public Block(Region parent) {
    this.parent = parent;
    this.label = "blk_" + parent.getNewBlockId();
  }

  public Region getParent() {
    return parent;
  }

  public String getLabel() {
    return label;
  }

  public List<Operation> getOperations() {
    return operations;
  }

  public void addOperation(Operation op) {
    operations.add(op);
  }

  public void removeOperation(Operation op) {
    operations.remove(op);
  }

  public void insertOperation(int index, Operation op) {
    operations.add(index, op);
  }

  public void insertOperationBefore(Operation op, Operation before) {
    int index = operations.indexOf(before);
    operations.add(index, op);
  }

  public void insertOperationAfter(Operation op, Operation after) {
    int index = operations.indexOf(after) + 1;
    operations.add(index, op);
  }
}

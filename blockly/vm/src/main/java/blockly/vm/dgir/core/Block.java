package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.List;

@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class,
  property = "parent")
public final class Block {
  private final String label;

  @JsonManagedReference
  private final List<Operation> operations = new ArrayList<>();

  @JsonBackReference
  public final Region parent;

  public Block(Region parent) {
    this.parent = parent;
    this.label = "blk_" + parent.getNewBlockId();
  }

  public String getLabel() {
    return label;
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

package blockly.vm.dgir.core;

import blockly.vm.dgir.core.opinterfaces.IControlFlowOp;
import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A block containing a list of {@link Operation}.
 * Blocks are always attached to a {@link Region}.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public final class Block extends IRObjectWithUseList<Block, BlockOperand> implements Serializable {
  /**
   * The value arguments of this block.
   * These are the input values to the block.
   */
  @JsonManagedReference
  private final List<BlockArgument> arguments = new ArrayList<>();
  /**
   * The operations contained in this block.
   * These are executed in order.
   */
  @JsonManagedReference
  private final List<Operation> operations = new ArrayList<>();

  @JsonBackReference
  private Region parent;

  public Block() {
  }

  public Block(List<BlockArgument> arguments,
               List<Operation> operations) {
    for (BlockArgument argument : arguments) {
      addArgument(argument);
    }
    for (Operation operation : operations) {
      addOperation(operation);
    }
  }

  public boolean hasTerminator(){
    if(operations.isEmpty()) return false;
    Operation lastOp = operations.getLast();
    return lastOp.getDetails().hasInterface(IControlFlowOp.class);
  }

  @JsonIgnore
  public Operation getTerminator(){
    assert hasTerminator() : "Block does not have a terminator operation.";
    return operations.getLast();
  }

  public List<BlockArgument> getArguments() {
    return Collections.unmodifiableList(arguments);
  }

  public void addArgument(BlockArgument argument) {
    assert argument != null : "Argument cannot be null.";
    assert argument.getParent() == null : "Argument already has a parent.";

    arguments.add(argument);
    argument.setParent(this);
  }

  public void removeArgument(BlockArgument argument) {
    assert argument != null : "Argument cannot be null.";
    assert argument.getParent() == this : "Argument does not belong to this block.";

    arguments.remove(argument);
    argument.setParent(null);
  }

  public List<Operation> getOperations() {
    return Collections.unmodifiableList(operations);
  }

  public void addOperation(Operation operation) {
    assert operation != null : "Operation cannot be null.";
    assert operation.getParent() == null : "Operation already has a parent.";

    operations.add(operation);
    operation.setParent(this);
  }

  public void removeOperation(Operation operation) {
    assert operation != null : "Operation cannot be null.";
    assert operation.getParent() == this : "Operation does not belong to this block.";

    operations.remove(operation);
    operation.setParent(null);
  }

  public int getArgumentIndex(BlockArgument blockArgument) {
    return arguments.indexOf(blockArgument);
  }

  public Region getParent() {
    return parent;
  }

  public void setParent(Region parent) {
    assert Utils.Caller.getCallingClass() == Region.class : "Assigning the parent of a block is only allowed from the Region class. Was called from " + Utils.Caller.getCallingClass().getName() ;
    assert parent == null || this.parent == null : "Block already has a parent. Unparent first before setting a new parent. (Use the region interface to unparent.)";

    this.parent = parent;
  }
}

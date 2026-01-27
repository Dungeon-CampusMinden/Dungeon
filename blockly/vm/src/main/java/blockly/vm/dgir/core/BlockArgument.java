package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * A value that represents an argument to a block.
 */
public class BlockArgument extends Value {
  /**
   * The block that owns this argument.
   */
  @JsonBackReference
  private Block parent = null;

  public BlockArgument() {}

  public BlockArgument(Type type) {
    super(type, Kind.BlockArgument);
  }

  public Block getParent() {
    return parent;
  }

  public void setParent(Block parent) {
    assert Utils.Caller.getCallingClass() == Block.class : "Assigning the parent of a block argument is only allowed from the Block class. Was called from " + Utils.Caller.getCallingClass().getName();
    assert this.parent == null || parent == null : "BlockArgument already has a parent.";

    this.parent = parent;
  }

  public int getIndex() {
    return getParent().getArgumentIndex(this);
  }
}

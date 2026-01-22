package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockArgument extends Value {
  /**
   * The block that owns this argument.
   */
  private Block owner;
  /**
   * The index of the argument in the block definition.
   */
  private int index;


  public BlockArgument() {
    super(null, Kind.OpResult);
    owner = null;
    index = -1;
  }

  public BlockArgument(Type type, Block owner, int index) {
    super(type, Kind.BlockArgument);
    this.owner = owner;
    this.index = index;
  }

  @JsonCreator
  public BlockArgument(@JsonProperty("type") Type type) {
    super(type, Kind.BlockArgument);
  }

  public Block getOwner() {
    return owner;
  }

  public void setOwner(Block owner) {
    this.owner = owner;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}

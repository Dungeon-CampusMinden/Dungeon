package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockArgument extends Value {
  /**
   * The index of the argument in the block definition.
   */
  private int index;
  /**
   * The block that owns this argument.
   */
  @JsonBackReference
  public Block owner;

  public BlockArgument(Type type, int index, Block owner) {
    super(type, Kind.BlockArgument);
    this.index = index;
    this.owner = owner;
  }

  @JsonCreator
  public BlockArgument(@JsonProperty("type") Type type) {
    super(type, Kind.BlockArgument);
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }
}

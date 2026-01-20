package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.List;

public final class Region {
  private int blockId = 0;

  @JsonManagedReference
  private List<Block> blocks = new ArrayList<>();

  @JsonBackReference
  public Operation parent;

  public Region(Operation parent){
    this.parent = parent;
  }

  @JsonCreator
  public Region() {
    this.parent = null;
  }

  public static Region CreateWithBlock(Operation parent) {
    var region = new Region(parent);
    region.getOrCreateDefaultBlock();
    return region;
  }

  @JsonIgnore
  public Block getOrCreateDefaultBlock() {
    return blocks.stream().findFirst().orElseGet(() -> {
      Block block = new Block(this);
      blocks.add(block);
      return block;
    });
  }

  @JsonIgnore
  public int getNewBlockId() {
    return blockId++;
  }
}

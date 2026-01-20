package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;
import java.util.List;

public final class Region {
  @JsonIgnore
  private int blockId = 0;
  @JsonManagedReference
  private final List<Block> blocks = new ArrayList<>();

  @JsonBackReference
  private final Operation parent;

  public Region(Operation parent){
    this.parent = parent;
  }

  public static Region CreateWithBlock(Operation parent) {
    var region = new Region(parent);
    region.getOrCreateDefaultBlock();
    return region;
  }

  public Operation getParent() {
    return parent;
  }

  @JsonIgnore
  public Block getOrCreateDefaultBlock() {
    return blocks.stream().findFirst().orElseGet(() -> {
      Block block = new Block(this);
      blocks.add(block);
      return block;
    });
  }

  public int getNewBlockId() {
    return blockId++;
  }
}

package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.List;

public final class Region {
  public List<Block> blocks;

  @JsonIgnore
  public Operation parent;

  public Region() {
    blocks = new ArrayList<>();
    parent = null;
  }

  @JsonCreator
  public Region(@JsonProperty("blocks") List<Block> blocks, @JsonProperty("parent") Operation parent) {

    this.blocks = blocks;
    this.parent = parent;
  }

  public static Region createWithBlock() {
    var region = new Region();
    region.getOrCreateDefaultBlock();
    return region;
  }

  @JsonIgnore
  public Block getOrCreateDefaultBlock() {
    return blocks.stream().findFirst().orElseGet(() -> {
      Block block = new Block();
      blocks.add(block);
      return block;
    });
  }


  private static int blockId = 0;
  @JsonIgnore
  public int getNewBlockId() {
    return blockId++;
  }
}

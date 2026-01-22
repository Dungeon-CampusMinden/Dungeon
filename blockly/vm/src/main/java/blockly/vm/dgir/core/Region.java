package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.util.StdConverter;

import java.util.ArrayList;
import java.util.List;

// JsonDeserialize is run after the full deserialization and used to update the references in the children
@JsonDeserialize(converter = RegionConverter.class)
public final class Region {
  private final List<Block> blocks;

  @JsonIgnore
  private Operation parent;

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

  public List<Block> getBlocks() {
    return blocks;
  }


  private static int blockId = 0;
  @JsonIgnore
  public int getNewBlockId() {
    return blockId++;
  }
}

/**
 * Used to update references post deserialization.
 */
class RegionConverter extends StdConverter<Region, Region> {
  @Override
  public Region convert(Region value) {
    for (var block : value.getBlocks()) {
      block.setParent(value);
    }
    return value;
  }
}

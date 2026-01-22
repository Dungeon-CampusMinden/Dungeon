package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.util.StdConverter;

import java.util.ArrayList;
import java.util.Collections;
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
      addBlock(block);
      return block;
    });
  }

  /**
   * Get the blocks in this region.
   *
   * @return An unmodifiable list of blocks.
   */
  public List<Block> getBlocks() {
    return Collections.unmodifiableList(blocks);
  }

  public void addBlock(Block block) {
    addBlockAt(blocks.size(), block);
  }

  public void addBlockAt(int index, Block block){
    blocks.add(index, block);
    block.owner = this;
  }

  public void addBlockBefore(Block block, Block before){
    addBlockAt(blocks.indexOf(before), block);
  }

  public void addBlockAfter(Block block, Block after){
    addBlockAt(blocks.indexOf(after) + 1, block);
  }

  public void removeBlock(Block block){
    removeBlockAt(blocks.indexOf(block));
  }

  public void removeBlockAt(int index){
    Block block = blocks.remove(index);
    if (block != null){
      block.owner = null;
    }
  }
}

/**
 * Used to update references post deserialization.
 */
class RegionConverter extends StdConverter<Region, Region> {
  @Override
  public Region convert(Region value) {
    for (var block : value.getBlocks()) {
      block.owner = value;
    }
    return value;
  }
}

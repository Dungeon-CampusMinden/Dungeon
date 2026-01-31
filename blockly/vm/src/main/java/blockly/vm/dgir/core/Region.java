package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Region {
  private final List<Block> blocks = new ArrayList<>();

  @JsonBackReference
  private final Operation parent;

  Region(Operation parent) {
    this.parent = parent;
  }

  public Region(List<Block> blocks, Operation parent) {
    this(parent);
    for (Block block : blocks) {
      addBlock(block);
    };
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

  public void addBlockAt(int index, Block block){
    assert block.getParent() == null : "Block is already part of a region.";
    assert index >= 0 && index <= blocks.size() : "Index out of bounds.";

    blocks.add(index, block);
    block.setParent(this);
  }

  public void addBlock(Block block) {
    addBlockAt(blocks.size(), block);
  }

  public void addBlockBefore(Block block, Block before){
    addBlockAt(blocks.indexOf(before), block);
  }

  public void addBlockAfter(Block block, Block after){
    addBlockAt(blocks.indexOf(after) + 1, block);
  }

  public void removeBlock(Block block){
    assert block.getParent() == this : "Block is not part of this region.";
    removeBlockAt(blocks.indexOf(block));
  }

  public void removeBlockAt(int index){
    assert index >= 0 && index < blocks.size() : "Index out of bounds.";

    Block block = blocks.remove(index);
    if (block != null){
      block.setParent(null);
    }
  }

  public Operation getParent() {
    return parent;
  }
}

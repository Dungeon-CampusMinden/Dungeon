package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Region {
  private final List<Block> blocks = new ArrayList<>();

  @JsonBackReference
  private Operation parent = null;

  public Region() {
  }

  public Region(List<Block> blocks, Operation parent) {
    for (Block block : blocks) {
      addBlock(block);
    };
    setParent(parent);
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

  public void setParent(Operation operation) {
    assert Utils.Caller.getCallingClass() == Operation.class : "Assigning the parent of a region is only allowed from the Operation class. Was called from " + Utils.Caller.getCallingClass().getName();
    assert this.parent == null || operation == null : "Region already has a parent. Unparent first before setting a new parent. (Use the operation interface to unparent.)";

    this.parent = operation;
  }
}

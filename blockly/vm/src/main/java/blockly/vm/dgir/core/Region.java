package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A region containing a list of {@link Block}.
 * Regions can be attached to an {@link Operation} but also freestanding in case they are supposed to be moved into an
 * operation. These regions are called "orphan regions" and do not contribute to the semantics of the program until they are
 * attached to an operation.
 * <p>
 * A region can take all blocks from another region using {@link #takeRegion(Region)} which is useful in case a region
 * needs to be build up before attaching it to an operation. In that case, an orphan region can be created, populated with blocks,
 * and then attached to an operation.
 * <p>
 * Regions always have at least one block, the entry block, which can be accessed using {@link #getEntryBlock()}. This block
 * is created automatically if no blocks are provided during construction. It is the block where execution starts when entering the region.
 * Regions can have multiple blocks, which can be added using {@link #addBlock(Block)} or removed using {@link #removeBlock(Block)}.
 * Regions maintain the parent-child relationship with their blocks, ensuring that each block knows which region it belongs to.
 * Regions themselves maintain a reference to their parent operation, if any, which can be accessed using {@link #getParent()}.
 * <p>
 * Regions are a fundamental part of the control flow structure in the DGIR, allowing for complex execution paths and nested operations.
 * They are essential for representing structured control flow such as loops, if-else statements, and function bodies
 * within the intermediate representation.
 * <p>
 * A region in DGIR can be conceptually represented as follows:
 * <pre>
 * {@code
 * Region {
 *   Block entryBlock {
 *    Operation1
 *    Operation2
 *    ...
 *    TerminatorOperation
 *  }
 *  Block anotherBlock {
 *    OperationA
 *    OperationB
 *    ...
 *    TerminatorOperation
 *  }
 *  ...
 * }
 * }
 * </pre>
 *
 * @author <a href="mailto:lasse.foster@hsbi.de">Lasse Foster</a>
 * @see Operation
 * @see Block
 */
public final class Region {
  private final List<Block> blocks = new ArrayList<>();

  @JsonIgnore
  private final Operation parent;

  public Region() {
    this.parent = null;
  }

  Region(Operation parent) {
    this.parent = parent;
  }

  @JsonCreator
  public Region(@JsonProperty(value = "blocks") List<Block> blocks) {
    this(blocks != null ? blocks : Collections.emptyList(), null);
  }

  public Region(List<Block> blocks, Operation parent) {
    this(parent);
    for (Block block : blocks) {
      addBlock(block);
    }
  }

  @JsonIgnore
  public Block getEntryBlock() {
    // Ensure that there is at least one block (the entry block).
    if (this.blocks.isEmpty()) {
      addBlock(new Block());
    }
    return blocks.getFirst();
  }

  /**
   * Get the blocks in this region.
   *
   * @return An unmodifiable list of blocks.
   */
  public List<Block> getBlocks() {
    return Collections.unmodifiableList(blocks);
  }

  public void addBlockAt(int index, Block block) {
    assert block.getParent() == null : "Block is already part of a region.";
    assert index >= 0 && index <= blocks.size() : "Index out of bounds.";

    blocks.add(index, block);
    block.setParent(this);
  }

  public void addBlock(Block block) {
    addBlockAt(blocks.size(), block);
  }

  public void addBlockBefore(Block block, Block before) {
    addBlockAt(blocks.indexOf(before), block);
  }

  public void addBlockAfter(Block block, Block after) {
    addBlockAt(blocks.indexOf(after) + 1, block);
  }

  public void removeBlock(Block block) {
    assert block.getParent() == this : "Block is not part of this region.";
    removeBlockAt(blocks.indexOf(block));
  }

  public void removeBlockAt(int index) {
    assert index >= 0 && index < blocks.size() : "Index out of bounds.";

    Block block = blocks.remove(index);
    if (block != null) {
      block.setParent(null);
    }
  }

  /**
   * Take all blocks from another region and add them to this region.
   *
   * @param other The other region to take blocks from.
   */
  public void takeRegion(Region other) {
    for (Block block : new ArrayList<>(other.blocks)) {
      other.removeBlock(block);
      addBlock(block);
    }
  }

  public Operation getParent() {
    return parent;
  }
}

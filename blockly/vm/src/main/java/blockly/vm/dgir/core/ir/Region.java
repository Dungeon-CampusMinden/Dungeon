package blockly.vm.dgir.core.ir;

import com.fasterxml.jackson.annotation.*;

import java.util.*;

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
@JsonPropertyOrder({"bodyValues", "blocks"})
public final class Region {
  @JsonIdentityReference(alwaysAsId = false)
  private final List<Block> blocks = new ArrayList<>();
  /**
   * Values that act like parameters/arguments visible only inside this region (e.g., block arguments for CFG nodes).
   */
  @JsonIdentityReference(alwaysAsId = false)
  private final List<Value> bodyValues;

  @JsonIgnore
  private final Operation parent;

  public Region() {
    this(null, List.of());
  }

  public Region(Operation parent) {
    this(parent, List.of());
  }

  public Region(Operation parent, List<Type> bodyValueTypes) {
    this.parent = parent;
    this.bodyValues = initBodyValues(bodyValueTypes);
  }

  private Region(List<Block> blocks, Operation parent, List<Value> bodyValues) {
    this.parent = parent;
    this.bodyValues = new ArrayList<>(bodyValues == null ? List.of() : bodyValues);
    for (Block block : blocks) {
      addBlock(block);
    }
  }

  @JsonCreator
  public static Region createRegion(@JsonProperty(value = "bodyValues") List<Value> bodyValues,
                                    @JsonProperty(value = "blocks") List<Block> blocks) {
    return new Region(blocks != null ? blocks : List.of(), null, bodyValues);
  }

  private static List<Value> initBodyValues(List<Type> bodyValueTypes) {
    List<Type> types = bodyValueTypes == null ? List.of() : bodyValueTypes;
    List<Value> values = new ArrayList<>(types.size());
    for (Type type : types) {
      values.add(new Value(Objects.requireNonNull(type, "body value type cannot be null")));
    }
    return values;
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
    // Make sure that both regions have the same body values or that this region is empty
    assert this.bodyValues.size() == other.bodyValues.size() : "Body values of regions must have the same size.";
    for (int i = 0; i < this.bodyValues.size(); i++) {
      assert this.bodyValues.get(i).getType().equals(other.bodyValues.get(i).getType()) : "Body value types of regions must match.";
    }

    for (Block block : new ArrayList<>(other.blocks)) {
      other.removeBlock(block);
      addBlock(block);
    }

    // Replace the value uses from the other regions body values with the body values of this region.
    for (int i = 0; i < this.bodyValues.size(); i++) {
      Value thisBodyValue = this.bodyValues.get(i);
      Value otherBodyValue = other.bodyValues.get(i);
      if (thisBodyValue != otherBodyValue)
        otherBodyValue.replaceAllUsesWith(thisBodyValue);
    }
  }

  public Operation getParent() {
    return parent;
  }

  @JsonIdentityReference(alwaysAsId = false)
  public List<Value> getBodyValues() {
    return bodyValues;
  }

  public Value getBodyValue(int index) {
    return bodyValues.get(index);
  }

  public int getBodyValueIndex(Value value) {
    return bodyValues.indexOf(value);
  }

  public void setBodyValues(List<Value> bodyValues) {
    // Make sure that the body values have the same size and types as the previous values or that none of the values are in use
    if (!this.bodyValues.isEmpty() && bodyValues.stream().anyMatch(v -> !v.getUses().isEmpty())) {
      assert this.bodyValues.size() == bodyValues.size() : "Body values of regions must have the same size.";
      for (int i = 0; i < this.bodyValues.size(); i++) {
        assert this.bodyValues.get(i).getType().equals(bodyValues.get(i).getType()) : "Body value types of regions must match.";
      }
    }

    // Replace the body values with the new ones
    if (!this.bodyValues.isEmpty())
      for (int i = 0; i < bodyValues.size(); i++) {
        Value oldValue = this.bodyValues.get(i);
        oldValue.replaceAllUsesWith(bodyValues.get(i));
      }

    this.bodyValues.clear();
    this.bodyValues.addAll(bodyValues);
  }

  /**
   * Finds the ancestor operation of the given operation that lies in this region. If the given operation itself lies in this region, it is returned.
   * This means that the operation that gets returned is always a first level descendant of this region, meaning it is not nested.
   *
   * @param op The operation to find the ancestor op of.
   * @return The same op if it lies in this region, otherwise the ancestor op of the op in this region. Null if no ancestor op exists in this region.
   */
  public Operation findAncestorOpInRegion(Operation op) {
    Operation currentOp = op;
    Region opRegion = currentOp.getParentRegion();
    while (opRegion != null) {
      if (opRegion == this)
        return currentOp;

      currentOp = opRegion.getParent();
      if (currentOp == null)
        return null;
      opRegion = currentOp.getParentRegion();
    }
    return null;
  }
}

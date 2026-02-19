package core.ir;

import com.fasterxml.jackson.annotation.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * A region containing an ordered list of {@link Block}s, attached to an {@link Operation}.
 * <p>
 * Regions can also be freestanding ("orphan" regions) while being built up, and then
 * transferred into an operation via {@link #takeRegion(Region)}.
 * <p>
 * Every region always has at least one block — the <em>entry block</em> — which is created
 * automatically if needed. Execution enters a region through this block.
 * <p>
 * Regions may carry <em>body values</em>: typed values that are visible inside the region
 * and act like block/region arguments (e.g. loop induction variables).
 * <pre>{@code
 * Region {
 *   Block entryBlock {
 *     Operation1
 *     ...
 *     TerminatorOperation
 *   }
 *   Block otherBlock { ... }
 * }
 * }</pre>
 *
 * @see Operation
 * @see Block
 */
@JsonPropertyOrder({"bodyValues", "blocks"})
public final class Region {

  // =========================================================================
  // Members
  // =========================================================================

  private final @NotNull List<Block> blocks = new ArrayList<>();

  /**
   * Values visible inside this region, acting as parameters/arguments
   * (e.g. the induction variable of a for-loop body).
   */
  @JsonIdentityReference(alwaysAsId = false)
  private final @NotNull List<Value> bodyValues;

  @JsonIgnore
  private final @NotNull Operation parent;

  // =========================================================================
  // Constructors
  // =========================================================================

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
    for (Block block : blocks)
      addBlock(block);
  }

  /**
   * Deserialization factory — body values and blocks are wired up by Jackson.
   */
  @JsonCreator
  public static Region createRegion(@JsonProperty(value = "bodyValues") List<Value> bodyValues,
                                    @JsonProperty(value = "blocks") List<Block> blocks) {
    return new Region(blocks != null ? blocks : List.of(), null, bodyValues);
  }

  // =========================================================================
  // Blocks
  // =========================================================================

  /**
   * Get the blocks in this region.
   *
   * @return An unmodifiable view of the block list.
   */
  @Contract(pure = true)
  public @NotNull @UnmodifiableView List<Block> getBlocks() {
    return Collections.unmodifiableList(blocks);
  }

  public void addBlock(@NotNull Block block) {
    addBlockAt(blocks.size(), block);
  }

  public void addBlockAt(int index, @NotNull Block block) {
    assert block.getParent().isEmpty() : "Block is already part of a region.";
    assert index >= 0 && index <= blocks.size() : "Index out of bounds.";
    blocks.add(index, block);
    block.setParent(this);
  }

  public void addBlockBefore(@NotNull Block block, @NotNull Block before) {
    addBlockAt(blocks.indexOf(before), block);
  }

  public void addBlockAfter(@NotNull Block block, @NotNull Block after) {
    addBlockAt(blocks.indexOf(after) + 1, block);
  }

  public void removeBlock(@NotNull Block block) {
    assert blocks.contains(block) : "Block is not part of this region.";
    removeBlockAt(blocks.indexOf(block));
  }

  public void removeBlockAt(int index) {
    assert index >= 0 && index < blocks.size() : "Index out of bounds.";
    Block block = blocks.remove(index);
    if (block != null)
      block.setParent(null);
  }

  /**
   * Ensure this region has at least one (entry) block.
   */
  public void ensureEntryBlock() {
    if (this.blocks.isEmpty())
      addBlock(new Block());
  }

  @JsonIgnore
  public @NotNull Block getEntryBlock() {
    ensureEntryBlock();
    return blocks.getFirst();
  }

  /**
   * Get the first operation in the entry block.
   *
   * @return The first operation in the entry block.
   */
  @JsonIgnore
  public @NotNull Operation getEntryOperation() {
    var operations = getEntryBlock().getOperations();
    assert !operations.isEmpty() : "Entry block must have at least one operation.";
    return operations.getFirst();
  }

  // =========================================================================
  // Body Values
  // =========================================================================

  public @NotNull List<Value> getBodyValues() {
    return bodyValues;
  }

  public Value getBodyValue(int index) {
    return bodyValues.get(index);
  }

  public int getBodyValueIndex(@NotNull Value value) {
    return bodyValues.indexOf(value);
  }

  /**
   * Replace the body values of this region with a new list.
   * Existing uses of the old values are redirected to the corresponding new values.
   *
   * @param bodyValues The new body values. Must match the existing list in size and types if
   *                   any of the current values are already in use.
   */
  public void setBodyValues(@NotNull List<Value> bodyValues) {
    if (!this.bodyValues.isEmpty() && bodyValues.stream().anyMatch(v -> !v.getUses().isEmpty())) {
      assert this.bodyValues.size() == bodyValues.size()
        : "Body values of regions must have the same size.";
      for (int i = 0; i < this.bodyValues.size(); i++) {
        assert this.bodyValues.get(i).getType().equals(bodyValues.get(i).getType())
          : "Body value types of regions must match.";
      }
    }

    if (!this.bodyValues.isEmpty())
      for (int i = 0; i < bodyValues.size(); i++)
        this.bodyValues.get(i).replaceAllUsesWith(bodyValues.get(i));

    this.bodyValues.clear();
    this.bodyValues.addAll(bodyValues);
  }

  // =========================================================================
  // Parent & Transfer
  // =========================================================================

  public @NotNull Operation getParent() {
    return parent;
  }

  /**
   * Move all blocks from {@code other} into this region.
   * Uses of {@code other}'s body values are replaced with the corresponding values from this region.
   *
   * @param other The region to drain. Must have matching body value types.
   */
  public void takeRegion(@NotNull Region other) {
    assert this.bodyValues.size() == other.bodyValues.size()
      : "Body values of regions must have the same size.";
    for (int i = 0; i < this.bodyValues.size(); i++) {
      assert this.bodyValues.get(i).getType().equals(other.bodyValues.get(i).getType())
        : "Body value types of regions must match.";
    }

    for (Block block : new ArrayList<>(other.blocks)) {
      other.removeBlock(block);
      addBlock(block);
    }

    for (int i = 0; i < this.bodyValues.size(); i++) {
      Value thisBodyValue = this.bodyValues.get(i);
      Value otherBodyValue = other.bodyValues.get(i);
      if (thisBodyValue != otherBodyValue)
        otherBodyValue.replaceAllUsesWith(thisBodyValue);
    }
  }

  // =========================================================================
  // Private Helpers
  // =========================================================================

  private static List<Value> initBodyValues(List<Type> bodyValueTypes) {
    List<Type> types = bodyValueTypes == null ? List.of() : bodyValueTypes;
    List<Value> values = new ArrayList<>(types.size());
    for (Type type : types)
      values.add(new Value(Objects.requireNonNull(type, "body value type cannot be null")));
    return values;
  }
}

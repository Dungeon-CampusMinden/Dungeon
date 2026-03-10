package dgir.core.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import dgir.core.IRObjectWithUseList;
import dgir.core.Utils;
import dgir.core.analysis.DotCFG;
import dgir.core.serialization.BlockIdGenerator;
import dgir.core.traits.ITerminator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A block containing an ordered list of {@link Operation}s. Blocks are always attached to a {@link
 * Region} and represent a sequence of operations with a single entry point and a terminating exit
 * operation.
 *
 * <p>The last operation in a block must be a terminator (see {@link ITerminator}), which defines
 * control flow to successor blocks via branches, jumps, or returns.
 *
 * <p>Blocks maintain the parent-child relationship with their contained operations and with their
 * enclosing {@link Region}. They are fundamental to representing unstructured control flow (CFG
 * nodes) in the DGIR.
 *
 * <pre>{@code
 * Block {
 *   Operation1
 *   Operation2
 *   ...
 *   TerminatorOperation
 * }
 * }</pre>
 *
 * @see Region
 * @see Operation
 * @see DotCFG
 */
@JsonIdentityInfo(generator = BlockIdGenerator.class)
public final class Block extends IRObjectWithUseList<Block, BlockOperand> implements Serializable {

  // =========================================================================
  // Members
  // =========================================================================

  /** Operations in this block, executed in order. The last must be a terminator. */
  private final @NotNull List<Operation> operations = new ArrayList<>();

  @JsonIgnore private @Nullable Region parent;

  // =========================================================================
  // Constructors
  // =========================================================================

  public Block() {}

  @JsonCreator
  public Block(@JsonProperty("operations") @Nullable List<Operation> operations) {
    if (operations != null) for (Operation operation : operations) addOperation(operation);
  }

  // =========================================================================
  // Operations
  // =========================================================================

  /**
   * Returns the operations in this block in execution order.
   *
   * <p>Modifying the returned list may cause undefined behavior. Use {@link #addOperation} and
   * {@link #removeOperation} to modify the block's operations.
   *
   * @return the operations list.
   */
  @Contract(pure = true)
  @JsonIgnore
  public @NotNull List<Operation> getOperationsRaw() {
    return operations;
  }

  /**
   * Returns the operations in this block in execution order.
   *
   * @return an unmodifiable view of the operations list.
   */
  @Contract(pure = true)
  public @NotNull @UnmodifiableView List<Operation> getOperations() {
    return Collections.unmodifiableList(operations);
  }

  /**
   * Append a typed op to this block, using its backing operation.
   *
   * @param op the op to add; must not already have a parent.
   * @param <OpT> the op type.
   * @return {@code op}, for convenient chaining.
   */
  public <OpT extends Op> @NotNull OpT addOperation(@NotNull OpT op) {
    addOperation(op.getOperation());
    return op;
  }

  /**
   * Append an operation to the end of this block.
   *
   * @param operation the operation to append; must not already have a parent.
   * @return {@code operation}, for convenient chaining.
   * @throws AssertionError if the operation already has a parent block.
   */
  public @NotNull Operation addOperation(@NotNull Operation operation) {
    assert operation.getParent().isEmpty() : "Operation already has a parent.";
    operations.add(operation);
    operation.setParent(this);
    return operation;
  }

  /**
   * Insert a typed op into this block at the given index, using its backing operation.
   *
   * @param op the op to insert; must not already have a parent.
   * @param index the index to insert at.
   * @param <OpT> the op type.
   * @return {@code op}, for convenient chaining.
   */
  public <OpT extends Op> @NotNull OpT addOperation(@NotNull OpT op, int index) {
    addOperation(op.getOperation(), index);
    return op;
  }

  /**
   * Insert an operation into this block at the given index.
   *
   * @param operation the operation to insert; must not already have a parent.
   * @param index the index to insert at.
   * @return {@code operation}, for convenient chaining.
   */
  public @NotNull Operation addOperation(@NotNull Operation operation, int index) {
    assert operation.getParent().isEmpty() : "Operation already has a parent.";
    assert index >= 0 && index <= operations.size() : "Index out of bounds.";
    operations.add(index, operation);
    operation.setParent(this);
    return operation;
  }

  /**
   * Remove an operation from this block and detach it from its parent.
   *
   * @param operation the operation to remove; must be owned by this block.
   * @throws AssertionError if the operation does not belong to this block.
   */
  public void removeOperation(@NotNull Operation operation) {
    assert operation.getParent().orElseThrow() == this : "Operation does not belong to this block.";
    operations.remove(operation);
    operation.setParent(null);
  }

  /**
   * Returns {@code true} if this block is non-empty and its last operation is a terminator.
   *
   * @return {@code true} if a terminator is present.
   */
  @Contract(pure = true)
  public boolean hasTerminator() {
    if (operations.isEmpty()) return false;
    return operations.getLast().hasTrait(ITerminator.class);
  }

  /**
   * Returns the terminator of this block, if present.
   *
   * @return the last operation if it is a terminator, otherwise empty.
   */
  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<Operation> getTerminator() {
    if (operations.isEmpty() || !operations.getLast().hasTrait(ITerminator.class))
      return Optional.empty();
    return Optional.of(operations.getLast());
  }

  // =========================================================================
  // Control Flow
  // =========================================================================

  /**
   * Get the successor blocks of this block as defined by the terminator operation.
   *
   * @return The successors of this block, or an empty list if there is no terminator.
   */
  @JsonIgnore
  @Contract(pure = true)
  public @NotNull List<Block> getSuccessors() {
    return !operations.isEmpty() ? operations.getLast().getSuccessors() : List.of();
  }

  /**
   * Get the predecessor blocks of this block via its use-list. A block B is a predecessor of this
   * block if some operation in B branches to this block.
   *
   * @return An unmodifiable set of predecessor blocks.
   */
  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Set<Block> getPredecessors() {
    return getUses().stream()
        .map(blockOperand -> blockOperand.getOwner().getParent())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toUnmodifiableSet());
  }

  // =========================================================================
  // Parent & Navigation
  // =========================================================================

  @Contract(pure = true)
  public @NotNull Optional<Region> getParent() {
    return Optional.ofNullable(parent);
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<Operation> getParentOperation() {
    return getParent().flatMap(Region::getParent);
  }

  /**
   * Set the parent region of this block. May only be called from {@link Region}.
   *
   * @param parent the new parent region, or {@code null} to detach.
   * @throws AssertionError if called from outside {@link Region}, or if this block already has a
   *     non-null parent and the new value is also non-null.
   */
  public void setParent(@Nullable Region parent) {
    assert Utils.getCallingClass() == Region.class
        : "Assigning the parent of a block is only allowed from the Region class. Was called from "
            + Utils.getCallingClass().getName();
    assert this.parent == null || parent == null
        : "Block already has a parent. Unparent first before setting a new parent. (Use the region interface to unparent.)";
    this.parent = parent;
  }

  /**
   * Get the index of this block in its parent region's block list.
   *
   * @return The index, or -1 if this block has no parent.
   */
  @Contract(pure = true)
  @JsonIgnore
  public int getIndex() {
    return getParent().map(region -> region.getBlocks().indexOf(this)).orElse(-1);
  }

  // =========================================================================
  // Queries
  // =========================================================================

  /**
   * Check whether operation {@code a} appears before operation {@code b} in this block.
   *
   * @param a The first operation.
   * @param b The second operation.
   * @return {@code true} if {@code a} is defined before {@code b}.
   */
  @Contract(pure = true)
  public boolean isBefore(@NotNull Operation a, @NotNull Operation b) {
    return operations.indexOf(a) < operations.indexOf(b);
  }

  // =========================================================================
  // Object
  // =========================================================================
  @Override
  public String toString() {
    return "Block[" + getIndex() + "] {" + operations.size() + '}';
  }
}

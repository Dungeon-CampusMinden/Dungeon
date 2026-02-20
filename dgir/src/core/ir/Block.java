package core.ir;

import core.IRObjectWithUseList;
import core.Utils;
import core.analysis.DotCFG;
import core.traits.ITerminator;
import com.fasterxml.jackson.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A block containing an ordered list of {@link Operation}s.
 * Blocks are always attached to a {@link Region} and represent a sequence of
 * operations with a single entry point and a terminating exit operation.
 * <p>
 * The last operation in a block must be a terminator (see {@link ITerminator}), which
 * defines control flow to successor blocks via branches, jumps, or returns.
 * <p>
 * Blocks maintain the parent-child relationship with their contained operations and with
 * their enclosing {@link Region}. They are fundamental to representing unstructured
 * control flow (CFG nodes) in the DGIR.
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
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public final class Block extends IRObjectWithUseList<Block, BlockOperand> implements Serializable {

  // =========================================================================
  // Members
  // =========================================================================

  /**
   * Operations in this block, executed in order. The last must be a terminator.
   */
  private final @NotNull List<Operation> operations = new ArrayList<>();

  @JsonIgnore
  private @Nullable Region parent;

  // =========================================================================
  // Constructors
  // =========================================================================

  public Block() {
  }

  @JsonCreator
  public Block(@JsonProperty("operations") @Nullable List<Operation> operations) {
    if (operations != null)
      for (Operation operation : operations)
        addOperation(operation);
  }

  // =========================================================================
  // Operations
  // =========================================================================

  public @NotNull @UnmodifiableView List<Operation> getOperations() {
    return Collections.unmodifiableList(operations);
  }

  public <OpT extends Op> @NotNull OpT addOperation(@NotNull OpT op) {
    addOperation(op.getOperation());
    return op;
  }

  public @NotNull Operation addOperation(@NotNull Operation operation) {
    assert operation.getParent().isEmpty() : "Operation already has a parent.";
    operations.add(operation);
    operation.setParent(this);
    return operation;
  }

  public void removeOperation(@NotNull Operation operation) {
    assert operation.getParent().orElseThrow() == this : "Operation does not belong to this block.";
    operations.remove(operation);
    operation.setParent(null);
  }

  public boolean hasTerminator() {
    if (operations.isEmpty()) return false;
    return operations.getLast().hasTrait(ITerminator.class);
  }

  @JsonIgnore
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
  public @NotNull List<Block> getSuccessors() {
    return !operations.isEmpty() ? operations.getLast().getSuccessors() : List.of();
  }

  /**
   * Get the predecessor blocks of this block via its use-list.
   * A block B is a predecessor of this block if some operation in B branches to this block.
   *
   * @return An unmodifiable set of predecessor blocks.
   */
  @JsonIgnore
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

  public @NotNull Optional<Region> getParent() {
    return Optional.ofNullable(parent);
  }

  @JsonIgnore
  public @NotNull Optional<Operation> getParentOperation() {
    return getParent().map(Region::getParent);
  }

  public void setParent(@Nullable Region parent) {
    assert Utils.Caller.getCallingClass() == Region.class
      : "Assigning the parent of a block is only allowed from the Region class. Was called from "
      + Utils.Caller.getCallingClass().getName();
    assert this.parent == null || parent == null
      : "Block already has a parent. Unparent first before setting a new parent. (Use the region interface to unparent.)";
    this.parent = parent;
  }

  /**
   * Get the index of this block in its parent region's block list.
   *
   * @return The index, or -1 if this block has no parent.
   */
  public int getIndex() {
    return getParent()
      .map(region -> region.getBlocks().indexOf(this))
      .orElse(-1);
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
  public boolean isBefore(@NotNull Operation a, @NotNull Operation b) {
    return operations.indexOf(a) < operations.indexOf(b);
  }
}

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
 * A block containing a list of {@link Operation}.
 * Blocks are always attached to a {@link Region}.
 * Each block has a list of operations that are executed in order. The last operation in the block must be a terminator operation,
 * which defines the control flow to other blocks, such as branches or jumps or returns to the parent operation/caller {@link DotCFG}.
 * <p>
 * A block can be moved between regions, which updates the parent region of the block accordingly.
 * Blocks maintain the parent-child relationship with their operations and block arguments, ensuring that each operation and argument knows which block it belongs to.
 * Blocks themselves maintain a reference to their parent region, which can be accessed using {@link #getParent()}.
 * Blocks are a fundamental part of the control flow structure in the DGIR, allowing for complex execution paths and nested operations.
 * They semantically represent a labeled sequence of operations with defined entry and exit points, essential for
 * representing unstructured control flow in the form of jumps and branches.
 * <p>
 * A block in DGIR can be conceptually represented as follows:
 * <pre>
 * {@code
 * Block {
 *  Operation1
 *  Operation2
 *  ...
 *  TerminatorOperation
 *  }
 * }
 * </pre>
 *
 * @author <a href="mailto:lasse.foster@hsbi.de">Lasse Foster</a>
 * @see Region
 * @see Operation
 * @see DotCFG
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public final class Block extends IRObjectWithUseList<Block, BlockOperand> implements Serializable {
  /**
   * The operations contained in this block.
   * These are executed in order.
   */
  private final @NotNull List<Operation> operations = new ArrayList<>();

  @JsonIgnore
  private @Nullable Region parent;

  public Block() {
  }

  @JsonCreator
  public Block(@JsonProperty("operations") @Nullable List<Operation> operations) {
    if (operations != null)
      for (Operation operation : operations) {
        addOperation(operation);
      }
  }

  public boolean hasTerminator() {
    if (operations.isEmpty()) return false;
    return operations.getLast().hasTrait(ITerminator.class);
  }

  @JsonIgnore
  public Optional<Operation> getTerminator() {
    if (operations.isEmpty() || !operations.getLast().hasTrait(ITerminator.class))
      return Optional.empty();
    return Optional.of(operations.getLast());
  }

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

  public @NotNull Optional<Region> getParent() {
    return Optional.ofNullable(parent);
  }

  @JsonIgnore
  public @NotNull Optional<Operation> getParentOperation() {
    return getParent().map(Region::getParent);
  }

  public void setParent(@Nullable Region parent) {
    assert Utils.Caller.getCallingClass() == Region.class : "Assigning the parent of a block is only allowed from the Region class. Was called from " + Utils.Caller.getCallingClass().getName();
    assert this.parent == null || parent == null : "Block already has a parent. Unparent first before setting a new parent. (Use the region interface to unparent.)";

    this.parent = parent;
  }

  /**
   * Get the successors of this block as defined by the terminator operation. This is a convenience method that delegates
   * to the terminator operation of this block.
   *
   * @return The successors of this block as defined by the terminator operation.
   */
  @JsonIgnore
  public @NotNull List<Block> getSuccessors() {
    return !operations.isEmpty() ? operations.getLast().getSuccessors() : List.of();
  }

  /**
   * Check if operation a is defined before operation b in this block. This is a convenience method that delegates to the list of operations in this block.
   *
   * @param a The first operation to compare.
   * @param b The second operation to compare.
   * @return {@code true} if operation a is defined before operation b in this block, {@code false} otherwise.
   */
  public boolean isBefore(@NotNull Operation a, @NotNull Operation b) {
    return operations.indexOf(a) < operations.indexOf(b);
  }

  /**
   * Get the predecessors of this block as defined by the use list of this block. This is a convenience method that
   * delegates to the use list of this block.
   *
   * @return The predecessor blocks of this block as defined by the use list of this block.
   */
  @JsonIgnore
  public @NotNull Set<Block> getPredecessors() {
    return getUses().stream()
      .map(blockOperand -> blockOperand.getOwner().getParent())
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toUnmodifiableSet());
  }

  public int getIndex() {
    return getParent()
      .map(region -> region.getBlocks().indexOf(this))
      .orElse(-1);
  }
}

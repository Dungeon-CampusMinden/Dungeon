package blockly.vm.dgir.core;

import blockly.vm.dgir.core.traits.IControlFlow;
import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
  private final List<Operation> operations = new ArrayList<>();

  @JsonIgnore
  private Region parent;

  public Block() {
  }

  @JsonCreator
  public Block(@JsonProperty("operations") List<Operation> operations) {
    if (operations != null)
      for (Operation operation : operations) {
        addOperation(operation);
      }
  }

  public boolean hasTerminator() {
    if (operations.isEmpty()) return false;
    Operation lastOp = operations.getLast();
    return lastOp.getDetails().hasTrait(IControlFlow.class);
  }

  @JsonIgnore
  public Operation getTerminator() {
    assert hasTerminator() : "Block does not have a terminator operation.";
    return operations.getLast();
  }

  public List<Operation> getOperations() {
    return Collections.unmodifiableList(operations);
  }

  public void addOperation(Op op) {
    assert op != null : "Op cannot be null.";
    addOperation(op.getOperation());
  }

  public void addOperation(Operation operation) {
    assert operation != null : "Operation cannot be null.";
    assert operation.getParent() == null : "Operation already has a parent.";

    operations.add(operation);
    operation.setParent(this);
  }

  public void removeOperation(Operation operation) {
    assert operation != null : "Operation cannot be null.";
    assert operation.getParent() == this : "Operation does not belong to this block.";

    operations.remove(operation);
    operation.setParent(null);
  }

  public Region getParent() {
    return parent;
  }

  @JsonIgnore
  public Operation getParentOperation(){
    return getParent().getParent();
  }

  public void setParent(Region parent) {
    assert Utils.Caller.getCallingClass() == Region.class : "Assigning the parent of a block is only allowed from the Region class. Was called from " + Utils.Caller.getCallingClass().getName();
    assert parent == null || this.parent == null : "Block already has a parent. Unparent first before setting a new parent. (Use the region interface to unparent.)";

    this.parent = parent;
  }

  /**
   * Get the successors of this block as defined by the terminator operation. This is a convenience method that delegates
   * to the terminator operation of this block.
   * @return The successors of this block as defined by the terminator operation.
   */
  @JsonIgnore
  public List<Block> getSuccessors() {
    return operations.getLast().getSuccessors();
  }

  /**
   * Check if operation a is defined before operation b in this block. This is a convenience method that delegates to the list of operations in this block.
   * @param a The first operation to compare.
   * @param b The second operation to compare.
   * @return {@code true} if operation a is defined before operation b in this block, {@code false} otherwise.
   */
  public boolean isBefore(Operation a, Operation b) {
      return operations.indexOf(a) < operations.indexOf(b);
  }
}

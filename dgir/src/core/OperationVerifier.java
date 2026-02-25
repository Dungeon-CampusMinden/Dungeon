package core;

import core.analysis.ReachingDefinitions;
import core.detail.OperationDetails;
import core.ir.*;
import core.traits.IIsolatedFromAbove;
import core.traits.INoTerminator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Validates the structural and semantic correctness of an {@link Operation} and, optionally, all
 * operations nested within it.
 *
 * <p>Verification is performed in a single iterative traversal using a work-list. Each work item
 * (operation or block) is visited twice — once on entry and once on exit — mirroring a depth-first
 * pre/post-order walk without recursion.
 *
 * <p>{@link IIsolatedFromAbove} operations are skipped during the main traversal and re-verified in
 * parallel on exit from their enclosing operation.
 */
public class OperationVerifier {

  // =========================================================================
  // Members
  // =========================================================================

  private final boolean recursive;

  // =========================================================================
  // Constructors
  // =========================================================================

  /**
   * @param recursive {@code true} to verify all nested operations; {@code false} to verify only the
   *     top-level operation and its immediate structure.
   */
  public OperationVerifier(boolean recursive) {
    this.recursive = recursive;
  }

  // =========================================================================
  // Public API
  // =========================================================================

  /**
   * Verify {@code operation} and (if recursive) all operations nested within it. After structural
   * verification passes, reaching-definition analysis is run over every region to ensure all value
   * uses have a visible definition.
   *
   * @param operation The operation to verify.
   * @return {@code true} if verification succeeds.
   */
  @Contract(pure = true)
  public boolean verify(@NotNull Operation operation) {
    if (!verifyOperation(operation)) return false;

    if (!operation.getRegions().isEmpty()) {
      List<ReachingDefinitions.MissingDefinition> missingDefinitions =
          ReachingDefinitions.validate(operation);
      if (!missingDefinitions.isEmpty()) {
        operation.emitError(
            "Operation has missing definitions: \n\t"
                + missingDefinitions.stream()
                    .map(ReachingDefinitions.MissingDefinition::message)
                    .reduce((a, b) -> a + "\n\t" + b)
                    .orElse(""));
        return false;
      }
    }

    return true;
  }

  // =========================================================================
  // Traversal
  // =========================================================================

  /**
   * Iterative pre/post-order traversal over all operations and blocks reachable from {@code
   * operation}, stopping region descent at {@link IIsolatedFromAbove} operations.
   *
   * @param operation The root operation.
   * @return {@code true} if every visited node passes verification.
   */
  @Contract(pure = true)
  private boolean verifyOperation(@NotNull Operation operation) {

    // Small union type to track whether the current item is a Block or an Operation
    class WorkItem {
      final @Nullable Operation op;
      final @Nullable Block block;
      boolean visited;

      WorkItem(@NotNull Operation op) {
        this.op = op;
        this.block = null;
        this.visited = false;
      }

      WorkItem(@NotNull Block block) {
        this.op = null;
        this.block = block;
        this.visited = false;
      }

      boolean verifyOnExit() {
        return op != null
            ? OperationVerifier.this.verifyOnExit(op)
            : OperationVerifier.this.verifyOnExit(Objects.requireNonNull(block));
      }

      boolean verifyOnEntry() {
        return op != null
            ? OperationVerifier.this.verifyOnEntry(op)
            : OperationVerifier.this.verifyOnEntry(Objects.requireNonNull(block));
      }
    }

    Deque<WorkItem> workList = new ArrayDeque<>();
    workList.add(new WorkItem(operation));

    while (!workList.isEmpty()) {
      WorkItem top = workList.peekLast();
      final boolean isExit = top.visited;
      top.visited = true;

      // ---- Second visit (exit) ----
      if (isExit) {
        if (!top.verifyOnExit()) return false;
        workList.removeLast();
        continue;
      }

      // ---- First visit (entry) ----
      if (!top.verifyOnEntry()) return false;

      // Enqueue children
      if (top.block != null) {
        // For blocks: enqueue ops that are not isolated-from-above (those are handled on exit)
        for (Operation op : top.block.getOperations()) {
          if (op.getRegions().isEmpty() || !op.hasTrait(IIsolatedFromAbove.class))
            workList.add(new WorkItem(op));
        }
        continue;
      }

      // For operations: enqueue all blocks of all regions in reverse order so they are
      // processed in forward order when popped from the stack
      if (recursive && top.op != null) {
        for (Region region : top.op.getRegions().reversed())
          for (Block block : region.getBlocks().reversed()) workList.add(new WorkItem(block));
      }
    }

    return true;
  }

  // =========================================================================
  // Entry / Exit Handlers
  // =========================================================================

  @Contract(pure = true)
  private boolean verifyOnEntry(@NotNull Operation operation) {
    // All operands must be non-null and have a non-null value
    for (ValueOperand operand : operation.getOperands()) {
      if (operand == null) {
        operation.emitError("Operation has null operand");
        return false;
      } else if (operand.getValue().isEmpty()) {
        operation.emitError("Operation has operand with null value");
        return false;
      }
    }

    // All attributes must be set and type-valid
    for (NamedAttribute attr : operation.getAttributes().values()) {
      if (attr.getAttribute().isEmpty()) {
        operation.emitError("Operation has attribute with null value: " + attr.getName());
        return false;
      }
      if (attr.getAttribute().get() instanceof TypedAttribute typedAttribute
          && !typedAttribute.getType().validate(typedAttribute.getStorage())) {
        operation.emitError(
            "Operation attribute '"
                + attr.getName()
                + "' with invalid value for storage type "
                + typedAttribute.getType().getParameterizedIdent()
                + ": "
                + typedAttribute.getStorage());
        return false;
      }
    }

    // Operation must be registered
    Optional<OperationDetails.Registered> details = operation.getDetails() instanceof OperationDetails.Registered registered ? Optional.of(registered) : Optional.empty();
    if (details.isEmpty()) {
      operation.emitError("Operation is not registered");
      return false;
    }

    // Verify traits first, then the operation's own verify()
    if (!details.get().verifyTraits(operation)) return false;
    if (!details.get().verify(operation)) {
      operation.emitError(
          "Operation failed verification through registered details.");
      return false;
    }

    // Region structural checks
    if (operation.getRegions().isEmpty()) return true;

    for (Region region : operation.getRegions()) {
      if (region.getBlocks().isEmpty()) continue;
      if (!region.getBlocks().getFirst().getPredecessors().isEmpty()) {
        operation.emitError("Entry block of region has predecessors.");
        return false;
      }
    }

    return true;
  }

  @Contract(pure = true)
  boolean verifyOnExit(@NotNull Operation op) {
    // Collect and re-verify all isolated-from-above child operations in parallel
    List<Operation> isolatedOps = new ArrayList<>();
    if (recursive)
      for (Region region : op.getRegions())
        for (Block block : region.getBlocks())
          for (Operation o : block.getOperations())
            if (!o.getRegions().isEmpty() && o.hasTrait(IIsolatedFromAbove.class))
              isolatedOps.add(o);

    AtomicBoolean failed = new AtomicBoolean(false);
    isolatedOps.parallelStream()
        .forEach(
            o -> {
              if (!verify(o)) failed.set(true);
            });
    return !failed.get();
  }

  @Contract(pure = true)
  private boolean verifyOnEntry(@NotNull Block block) {
    if (block.getOperations().isEmpty()) {
      if (isValidWithoutTerminator(block)) return true;
      Optional<Operation> parentOp = block.getParentOperation();
      if (parentOp.isPresent())
        parentOp.get().emitError("Block must end in a terminator operation");
      else System.err.println("Error: Block must end in a terminator operation");
      return false;
    }

    if (!isValidWithoutTerminator(block) && !block.hasTerminator()) {
      block.getOperations().getLast().emitError("Block does not have a terminator");
      return false;
    }

    // No operation other than the last may branch
    for (Operation op : block.getOperations()) {
      if (op != block.getOperations().getLast() && !op.getSuccessors().isEmpty()) {
        block
            .getOperations()
            .getLast()
            .emitError("Branching out of block must be the last operation in the block");
        return false;
      }
    }

    return true;
  }

  @Contract(pure = true)
  boolean verifyOnExit(@NotNull Block block) {
    // All successors must belong to the same region as this block
    for (Block successor : block.getSuccessors()) {
      if (!successor.getParent().equals(block.getParent())) {
        block.getOperations().getLast().emitError("Branching to block of a different region");
        return false;
      }
    }

    if (isValidWithoutTerminator(block)) return true;

    if (!block.hasTerminator()) {
      block.getOperations().getLast().emitError("Block does not have a terminator");
      return false;
    }

    return true;
  }

  // =========================================================================
  // Private Helpers
  // =========================================================================

  /**
   * Return {@code true} if {@code block} is allowed to have no terminator. This is the case when
   * the block is the sole block in a region whose parent operation carries the {@link
   * INoTerminator} trait.
   */
  @Contract(pure = true)
  private boolean isValidWithoutTerminator(@NotNull Block block) {
    return block
        .getParent()
        .map(
            parentRegion ->
                parentRegion.getBlocks().size() == 1
                    && parentRegion
                        .getParent()
                        .map(operation -> operation.hasTrait(INoTerminator.class))
                        .orElse(true))
        .orElse(false);
  }
}

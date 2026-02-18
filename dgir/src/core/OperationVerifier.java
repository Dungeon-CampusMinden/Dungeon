package core;

import core.analysis.ReachingDefinitions;
import core.detail.RegisteredOperationDetails;
import core.ir.*;
import core.traits.IIsolatedFromAbove;
import core.traits.INoTerminator;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class OperationVerifier {
  private final boolean recursive;

  public OperationVerifier(boolean recursive) {
    this.recursive = recursive;
  }

  public boolean verify(Operation operation) {
    if (!verifyOperation(operation)) {
      return false;
    }

    // At this point structurally the operation is valid and we can now check that all value uses are valid and
    // reaching definitions are correct.
    if (!operation.getRegions().isEmpty()) {
      List<ReachingDefinitions.MissingDefinition> missingDefinitions = ReachingDefinitions.validate(operation);
      if (!missingDefinitions.isEmpty()) {
        operation.emitError("Operation has missing definitions: \n\t" +
          missingDefinitions.stream()
            .map(ReachingDefinitions.MissingDefinition::message)
            .reduce((a, b) -> a + "\n\t" + b).orElse(""));
        return false;
      }
    }

    return true;
  }


  /**
   * Verify the properties stopping region recursion at any isolated from above operations
   *
   * @return true if the operation is valid, false otherwise.
   */
  private boolean verifyOperation(Operation operation) {
    // Union type to represent work items
    class WorkItem {
      final Operation op;
      final Block block;
      boolean visited;

      WorkItem(Operation op) {
        assert op != null : "Operation cannot be null.";
        this.op = op;
        this.block = null;
        this.visited = false;
      }

      WorkItem(Block block) {
        assert block != null : "Block cannot be null.";
        this.op = null;
        this.block = block;
        this.visited = false;
      }
    }

    Deque<WorkItem> workList = new ArrayDeque<>();
    workList.add(new WorkItem(operation));

    while (!workList.isEmpty()) {
      WorkItem top = workList.peekLast();

      final boolean isExit = top.visited;
      top.visited = true;

      // 2nd visit of this work item
      if (isExit) {
        // Lambda to visit either a block or an operation on exit
        Function<WorkItem, Boolean> visitOnExit = (WorkItem item) -> {
          if (item.op != null)
            return verifyOnExit(item.op);
          return verifyOnExit(item.block);
        };
        if (!visitOnExit.apply(top))
          return false;
        workList.removeLast();
        continue;
      }

      // 1st visit of this work item
      // Lambda to visit either a block or an operation on entry
      Function<WorkItem, Boolean> visitOnEntry = (WorkItem item) -> {
        if (item.op != null)
          return verifyOnEntry(item.op);
        return verifyOnEntry(item.block);
      };

      if (!visitOnEntry.apply(top))
        return false;

      // If we are in a block add all operations to the work list
      if (top.block != null) {
        // Skip isolate from above operations
        for (Operation op : top.block.getOperations()) {
          if (op.getRegions().isEmpty() || !op.hasTrait(IIsolatedFromAbove.class))
            workList.add(new WorkItem(op));
        }
        continue;
      }

      Operation currentOp = top.op;
      if (recursive)
        for (Region region : currentOp.getRegions().reversed())
          for (Block block : region.getBlocks().reversed())
            workList.add(new WorkItem(block));
    }

    return true;
  }

  private boolean isValidWithoutTerminator(Block block) {
    if (block.getParent() == null)
      return true;
    if (block.getParent().getBlocks().size() > 1)
      return false;
    return block.getParentOperation().hasTrait(INoTerminator.class);
  }

  private boolean verifyOnEntry(Operation operation) {
    // Check that operands are non null and structurally ok
    for (ValueOperand operand : operation.getOperands())
      if (operand == null) {
        operation.emitError("Operation has null operand");
        return false;
      } else if (operand.getValue() == null) {
        operation.emitError("Operation has operand with null value");
        return false;
      }

    // Verify that all of the attributes of this operation are valid
    for (NamedAttribute attr : operation.getAttributes().values())
      if (attr.getAttribute() == null) {
        operation.emitError("Operation has attribute with null value: " + attr.getName());
        return false;
      }
      // Verify that the attribute value is valid and of the correct type in case it is typed
      else if (attr.getAttribute() instanceof TypedAttribute typedAttribute
        && !typedAttribute.getType().validate(attr.getAttribute().getStorage())) {
        operation.emitError("Operation attribute '" + attr.getName() + "' with invalid value for storage type " + typedAttribute.getType().getParameterizedIdent() + ": " + attr.getAttribute().getStorage());
        return false;
      }

    Optional<RegisteredOperationDetails> details = operation.getDetails().asRegisteredDetails();
    if (details.isEmpty()) {
      operation.emitError("Operation is not registered");
      return false;
    }

    // Verify that all the operation traits are valid
    if (!details.get().verifyTraits(operation))
      return false;

    if (!details.get().verify(operation)) {
      operation.emitError("Operation failed verification through registered details of operation " + operation.getDetails().getIdent());
      return false;
    }

    // If this operation has no regions, we are done with verification at this point and can skip the region checks
    if (operation.getRegions().isEmpty())
      return true;

    // Verify that child regions are ok
    for (Region region : operation.getRegions()) {
      if (region.getBlocks().isEmpty())
        continue;

      // Verify that the first block has no predecessors
      if (!region.getBlocks().getFirst().getPredecessors().isEmpty()) {
        operation.emitError("Entry block of region has predecessors.");
        return false;
      }
    }

    return true;
  }

  private boolean verifyOnExit(Operation op) {
    List<Operation> operationsWithIsolatedRegions = new ArrayList<>();
    if (recursive)
      for (Region region : op.getRegions())
        for (Block block : region.getBlocks())
          for (Operation o : block.getOperations())
            if (!o.getRegions().isEmpty() &&
              o.hasTrait(IIsolatedFromAbove.class))
              operationsWithIsolatedRegions.add(o);

    AtomicBoolean opFailedVerify = new AtomicBoolean(false);
    operationsWithIsolatedRegions.parallelStream().forEach(o -> {
      if (!verify(o))
        opFailedVerify.set(true);
    });
    // Registered verification already performed on entry
    return !opFailedVerify.get();
  }

  private boolean verifyOnEntry(Block block) {
    // Verify that this block has a terminator
    if (block.getOperations().isEmpty()) {
      if (isValidWithoutTerminator(block))
        return true;

      Operation parentOp = block.getParentOperation();
      if (parentOp != null)
        parentOp.emitError("Block must end in a terminator operation");
      else
        System.err.println("Error: Block must end in a terminator operation");
      return false;
    }

    boolean allowNoTerminator = isValidWithoutTerminator(block);
    if (!allowNoTerminator && !block.hasTerminator()) {
      block.getOperations().getLast().emitError("Block does not have a terminator");
      return false;
    }

    // Check each operation and make sure there are no branches out of the middle of this block
    for (Operation op : block.getOperations()) {
      if (op != block.getOperations().getLast() && !op.getSuccessors().isEmpty()) {
        block.getOperations().getLast().emitError("Branching out of block must be the last operation in the block");
        return false;
      }
    }

    return true;
  }

  private boolean verifyOnExit(Block block) {
    // Verify that this block is not branching to a block of a different region
    for (Block successor : block.getSuccessors())
      if (successor.getParent() != block.getParent()) {
        block.getOperations().getLast().emitError("Branching to block of a different region");
        return false;
      }

    if (isValidWithoutTerminator(block))
      return true;

    // Verify that this block has a terminator
    if (!block.hasTerminator()) {
      block.getOperations().getLast().emitError("Block does not have a terminator");
      return false;
    }

    return true;
  }
}

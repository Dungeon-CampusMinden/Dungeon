package core.analysis;

import core.*;
import core.ir.Block;
import core.ir.BlockOperand;
import core.ir.Operation;
import core.ir.Region;
import core.traits.ISymbolUser;
import core.traits.IControlFlow;
import core.traits.ITerminator;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.Optional;

public class DetailedCFG {
  /**
   * A transition in the CFG.
   */
  public static class Transition extends DefaultEdge {
    public enum Type {
      /**
       * The regular transition from one operation to another
       */
      OP_OP,
      /**
       * The transition from an operation to a block (e.g. control flow ops, function calls)
       */
      OP_BLOCK,
      /**
       * The transition from a block to its first operation
       */
      BLOCK_OP,
      /**
       * The transition from a block to an operation (e.g. return statements, yield statements)
       */
      RETURN,
    }

    public enum Context {
      DEFAULT,
      FUNCTION_CALL,
      CONTROL_FLOW,
      STRUCTURED_CONTROL_FLOW,
      ENTRY
    }

    private final Type type;
    private final Context context;

    /**
     * Creates a new transition of the given type.
     *
     * @param type The type of the transition.
     */
    public Transition(Type type, Context context) {
      this.type = type;
      this.context = context;
    }

    /**
     * Returns the type of this transition.
     *
     * @return The type of this transition.
     */
    public Type getType() {
      return type;
    }

    public Context getContext() {
      return context;
    }

    @Override
    public String toString() {
      // Get the transition type string
      String base = switch (type) {
        case OP_OP -> "OP -> OP";
        case OP_BLOCK -> "OP -> BLOCK";
        case BLOCK_OP -> "BLOCK -> OP";
        case RETURN -> "RETURN";
        default -> "UNKNOWN";
      };
      // Add the context if it is not the default context
      return context == Context.DEFAULT ? base : base + "\n(" + context + ")";
    }
  }

  /**
   * A node in the CFG.
   * This is a wrapper around either a Block or an Operation, depending on the context.
   *
   */
  public static class CfgNode {
    private final Block block;
    private final Operation operation;

    public CfgNode(Block block) {
      this.block = block;
      this.operation = null;
    }

    public CfgNode(Operation operation) {
      this.operation = operation;
      this.block = null;
    }

    public boolean isBlock() {
      return block != null;
    }

    public boolean isOperation() {
      return operation != null;
    }

    public Block getBlock() {
      return block;
    }

    public Operation getOperation() {
      return operation;
    }

    @Override
    public int hashCode() {
      if (operation != null)
        return operation.hashCode();
      else if (block != null)
        return block.hashCode();
      else
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (obj instanceof CfgNode other) {
        if (operation != null) {
          return operation.equals(other.operation);
        } else if (block != null) {
          return block.equals(other.block);
        }
      }
      return false;
    }

    /**
     * Create a descriptive string representation of this operation.
     * Information include the ident of the operation, the number of operands and their type, the return type,
     * the number of regions and blocks.
     *
     * @param operation The operation to describe.
     * @return A descriptive string representation of this operation.
     */
    private String toString(Operation operation) {
      return operation.getDetails().getIdent() +
        "\n(" +
        "Operands: " + operation.getOperands().size() + ", " +
        "Output: " + (operation.getOutput() != null ? operation.getOutput().getType().getParameterizedIdent() : "void") + ", " +
        "Regions: " + operation.getRegions().size() +
        ")";
    }

    public String toString(Block block) {
      return "Block\n" +
        "(" +
        "Operations: " + block.getOperations().size() +
        ")";
    }

    @Override
    public String toString() {
      if (isBlock()) {
        assert block != null;
        return toString(block);
      } else {
        assert operation != null;
        return "Operation: " + toString(operation);
      }
    }
  }

  /**
   * Constructs a control flow graph for the given operation with all the transitions inside nested regions included.
   * Calling this function will only return the cfg for the regions of this operation and not the following operations.
   *
   * @param cfg       The cfg to populate, or null to create a new one.
   * @param operation The operation to construct the cfg for.
   * @return The constructed cfg.
   */
  public static Graph<CfgNode, Transition> getCfg(Graph<CfgNode, Transition> cfg, Operation operation) {
    if (cfg == null) {
      cfg = GraphTypeBuilder
        .<CfgNode, Transition>directed()
        .allowingMultipleEdges(false)
        .allowingSelfLoops(true)
        .buildGraph();
    }

    if (cfg.containsVertex(new CfgNode(operation))) {
      return cfg;
    }

    cfg.addVertex(new CfgNode(operation));
    // Go over all regions of this operation
    for (Region operationRegion : operation.getRegions()) {
      // Get the cfg for the region and add it to the cfg
      // Since the first block of a region is always the entry block, we only need to provide the first block here
      getCfg(cfg, operationRegion.getBlocks().getFirst());
      // Add the edge from the operation to its entry blocks
      cfg.addEdge(
        new CfgNode(operation),
        new CfgNode(operationRegion.getBlocks().getFirst()),
        new Transition(
          Transition.Type.OP_BLOCK,
          operation.hasTrait(IControlFlow.class) ?
            IControlFlow.isStructured(operation) ?
              Transition.Context.STRUCTURED_CONTROL_FLOW
              : Transition.Context.ENTRY
            : Transition.Context.ENTRY
        )
      );
    }
    return cfg;
  }

  /**
   * Constructs a control flow graph for the given operation with all the transitions inside nested regions included.
   *
   * @param cfg   The cfg to populate, or null to create a new one.
   * @param block The block to construct the cfg for.
   * @return The constructed cfg.
   */
  public static Graph<CfgNode, Transition> getCfg(Graph<CfgNode, Transition> cfg, Block block) {
    if (cfg == null) {
      cfg = GraphTypeBuilder
        .<CfgNode, Transition>directed()
        .allowingMultipleEdges(false)
        .allowingSelfLoops(true)
        .buildGraph();
    }

    if (cfg.containsVertex(new CfgNode(block))) {
      return cfg;
    }

    cfg.addVertex(new CfgNode(block));
    Operation lastOp = null;
    // Go over all operations contained in this block
    for (Operation blockOperation : block.getOperations()) {
      // If this operation is a terminator, we reached the end of this block and return control to the owning operation
      if (blockOperation.getDetails().hasTrait(ITerminator.class)) {
        // Insert the return transition
        cfg.addVertex(new CfgNode(blockOperation));
        /*
        Go from the return operation to the parent operation which hold the region this block is in.
        e.g.
        cfg.if(%condition) {
          cfg.return(); // Returns control flow back to cfg.if
        }
         */
        cfg.addEdge(
          new CfgNode(blockOperation),
          new CfgNode(block.getParent().getParent()),
          new Transition(Transition.Type.RETURN, Transition.Context.FUNCTION_CALL));
      }
      // If this operation is a control flow operation, generate its subgraph
      else if (blockOperation.getDetails().hasTrait(IControlFlow.class)) {
        // Check if this operation is a structured control flow operation (e.g. if-else)
        if (IControlFlow.isStructured(blockOperation)) {
          // Add the structured control flow operation's cfg to the graph
          cfg = getCfg(cfg, blockOperation);
        }
        // If it does not have regions we know its a simple unstructured control flow operation
        else {
          // This operation i a function call an therefore we need to build its subgraph anyways.
          ISymbolUser symbolUser;
          if ((symbolUser = blockOperation.asTrait(ISymbolUser.class)) != null) {
            cfg.addVertex(new CfgNode(blockOperation));

            Optional<Operation> targetOp = SymbolTable.lookupSymbolInNearestTable(
              blockOperation,
              symbolUser.getSymbolRefAttribute().getValue());
            // If we can find the target operation we create the function call edge
            if (targetOp.isPresent()) {
              // Get the cfg for the function call and add it to the graph
              cfg = getCfg(cfg, targetOp.get());
              // Add the edge from the call op to the called op
              cfg.addEdge(
                new CfgNode(blockOperation),
                new CfgNode(targetOp.get()),
                new Transition(Transition.Type.OP_OP, Transition.Context.FUNCTION_CALL)
              );
            } else {
              for (BlockOperand successorOperand : blockOperation.getBlockOperands()) {
                Block successorBlock = successorOperand.getValue();
                // Add the successor blocks cfg to the graph
                getCfg(cfg, successorBlock);

                // Add the edge from this operation to the successor block
                cfg.addEdge(
                  new CfgNode(blockOperation),
                  new CfgNode(successorBlock),
                  new Transition(Transition.Type.OP_BLOCK, Transition.Context.CONTROL_FLOW));
              }
            }
          }
        }
      }
      // Handle regular operations
      else {
        cfg.addVertex(new CfgNode(blockOperation));
      }

      // Connect the last operation to this operation or to the block if it is the first operation
      if (lastOp != null) {
        // This edge is a regular operation edge
        cfg.addEdge(
          new CfgNode(lastOp),
          new CfgNode(blockOperation),
          new Transition(Transition.Type.OP_OP, Transition.Context.DEFAULT));
      } else {
        // This edge is a block entry edge
        cfg.addEdge(
          new CfgNode(block),
          new CfgNode(blockOperation),
          new Transition(Transition.Type.BLOCK_OP, Transition.Context.ENTRY));
      }

      lastOp = blockOperation;
    }

    return cfg;
  }
}

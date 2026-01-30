package blockly.vm.dgir.core;

import blockly.vm.dgir.core.opinterfaces.IControlFlowOp;
import blockly.vm.dgir.core.opinterfaces.ITerminator;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

public class CFG {
  public static Graph<Block, DefaultEdge> getCfgFor(Graph<Block, DefaultEdge> cfg, Block block) {
    if (cfg == null) {
      cfg = GraphTypeBuilder
        .<Block, DefaultEdge>directed()
        .allowingMultipleEdges(false)
        .allowingSelfLoops(true)
        .buildGraph();
    }

    // Avoid recursing infinitely
    if (cfg.containsVertex(block)) return cfg;

    cfg.addVertex(block);

    // Go over all operations contained in this block
    for (Operation blockOperation : block.getOperations()) {
      // If this operation is a terminator, we reached the end of this block and return control to the caller
      if (blockOperation.getDetails().hasInterface(ITerminator.class)) {
        return cfg;
      }
      // If this operation is a control flow operation, generate its subgraph
      if (blockOperation.getDetails().hasInterface(IControlFlowOp.class)) {
        // Check if this operation is a structured control flow operation (e.g. if-else)
        if (blockOperation.getRegions().size() > 1) {
          for (Region operationRegion : blockOperation.getRegions()) {
            // Add the subgraph to the cfg
            getCfgFor(cfg, operationRegion.getBlocks().getFirst());
          }
          // Add the connection to the entry block of the nested region
          cfg.addEdge(block, blockOperation.getRegions().getFirst().getBlocks().getFirst());
        }
        // If it does not have regions we now its a simple unstructured control flow operation and can
        // add the block and successor block to the cfg
        else {
          for (BlockOperand successorOperand : blockOperation.getBlockOperands()) {
            Block successorBlock = successorOperand.getValue();
            // Add the successor blocks cfg to the graph
            getCfgFor(cfg, successorBlock);

            // Add the edge from this block to the successor block
            cfg.addEdge(block, successorBlock);
          }
        }
      }
    }

    return cfg;
  }
}

package blockly.vm.dgir.core.analysis;

import blockly.vm.dgir.core.ir.Block;
import blockly.vm.dgir.core.ir.BlockOperand;
import blockly.vm.dgir.core.ir.Operation;
import blockly.vm.dgir.core.ir.Region;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;

public class DotCFG {
  private DotCFG() {
  }

  public static Pair<Graph<Operation, DefaultEdge>, Cluster> buildCfg(Operation root) {
    GraphBuilder builder = new GraphBuilder(root);
    builder.processOperation(root);
    return builder.getCfg();
  }

  /**
   * A cluster is a logical grouping of multiple operations within the CFG. It can alternatively be thought of as a block, or a region.
   * Clusters can be nested within each other to represent hierarchical structures in the CFG.
   * A cluster also contains the first operation of each child cluster since it needs to draw a connection to that operation.
   * An empty cluster represents a region and can only contain other clusters.
   */
  public static class Cluster {
    private final Operation owner;
    private final Cluster parent;
    private final List<Operation> operations = new ArrayList<>();
    private final List<Cluster> children = new ArrayList<>();

    public Cluster(Operation owner, Cluster parent) {
      this.owner = owner;
      this.parent = parent;
    }

    public List<Operation> getOperations() {
      return operations;
    }

    public void addOperation(Operation op) {
      operations.add(op);
    }

    public List<Cluster> getChildren() {
      return children;
    }

    public Cluster addChild(Cluster child) {
      children.add(child);
      return child;
    }

    public Operation getOwner() {
      return owner;
    }

    public Cluster getParent() {
      return parent;
    }

    /**
     * Returns a subgraph of the given CFG that includes only the operations in this cluster.
     * The subgraph will include all edges from the original CFG that connect operations in this cluster.
     * Note that this method does not include any operations from child clusters, only the operations directly in this cluster.
     *
     * @param cfg The original CFG to mask.
     * @return A subgraph of the given CFG that includes only the operations in this cluster.
     */
    public AsSubgraph<Operation, DefaultEdge> getMasked(Graph<Operation, DefaultEdge> cfg) {
      // The list of operations to include in the subgraph including the first operation of each child entry block
      List<Operation> withEntry = new ArrayList<>(operations);
      for (Cluster regionChild : children) {
        // Check if the region is empty and skip it if it is
        if (regionChild.getChildren().isEmpty())
          continue;
        Cluster entryBlock = regionChild.getChildren().getFirst();
        if (!entryBlock.getOperations().isEmpty())
          withEntry.add(entryBlock.getOperations().getFirst());
      }
      return new AsSubgraph<>(cfg, Set.copyOf(withEntry));
    }

    /**
     * Recursively creates a dot graph representation of this cluster and its children.
     * The graph type of the children graphs are replaced with 'subgraph' and a unique label is assigned to each cluster.
     *
     * @param cfg The original CFG to mask.
     * @return A dot graph representation of this cluster and its children.
     */
    public String toDotString(Graph<Operation, DefaultEdge> cfg) {
      List<String> subgraphs = new ArrayList<>();
      int clusterCounter = 0;
      for (Cluster child : children) {
        subgraphs.add(child.toDotString(cfg));
        // Replace the graph type of the child cluster with 'subgraph' and add a unique label
        subgraphs.set(subgraphs.size() - 1, subgraphs.getLast().replace("digraph", "subgraph"));
        subgraphs.set(subgraphs.size() - 1, subgraphs.getLast().replace("subgraph G", "subgraph cluster_" + clusterCounter++));
      }
      class OpVertexIdent implements Function<Operation, String> {
        @Override
        public String apply(Operation op) {
          return op.getDetails().getIdent().replace(".", "_") + "_" + op.hashCode();
        }
      }
      DOTExporter<Operation, DefaultEdge> exporter = new DOTExporter<>(new OpVertexIdent());
      StringWriter sb = new StringWriter();
      exporter.exportGraph(getMasked(cfg), sb);

      // Add the subgraphs to the main graph after the opening brace of the main graph
      StringBuilder mainGraph = new StringBuilder(sb.toString());
      mainGraph.insert(mainGraph.indexOf("{") + 2, String.join("\n", subgraphs).indent(1));
      return mainGraph.toString();
    }
  }

  private static class GraphBuilder {
    private Graph<Operation, DefaultEdge> cfg;
    private final Cluster rootCluster;

    private Cluster currentCluster;

    GraphBuilder(Operation root) {
      cfg = GraphTypeBuilder
        .<Operation, DefaultEdge>directed()
        .edgeClass(DefaultEdge.class)
        .vertexClass(Operation.class)
        .allowingSelfLoops(true)
        .allowingMultipleEdges(true)
        .buildGraph();

      rootCluster = new Cluster(root, null);
      currentCluster = rootCluster;
    }

    Pair<Graph<Operation, DefaultEdge>, Cluster> getCfg() {
      return Pair.of(cfg, rootCluster);
    }

    /**
     * Add this operation to the graph and all of its regions.
     *
     * @param op The operation to process
     */
    void processOperation(Operation op) {
      cfg.addVertex(op);
      currentCluster.addOperation(op);

      // If there are no regions in this operation end the generation here
      if (op.getRegions().isEmpty()) return;

      for (Region region : op.getRegions()) {
        processRegion(region);
      }
      // Add edges from this operation to the first operation in each region entry block
      for (Region region : op.getRegions()) {
        if (!region.getBlocks().isEmpty() && !region.getEntryBlock().getOperations().isEmpty()) {
          cfg.addEdge(op, region.getEntryBlock().getOperations().getFirst());
        }
      }
    }

    private void processRegion(Region region) {
      // Open a new cluster for the region
      currentCluster = currentCluster.addChild(new Cluster(currentCluster.owner, currentCluster));
      for (Block block : region.getBlocks()) {
        processBlock(block);
      }
      // Close the region cluster
      currentCluster = currentCluster.getParent();
    }

    private void processBlock(Block block) {
      // Open a new cluster for the block
      currentCluster = currentCluster.addChild(new Cluster(currentCluster.owner, currentCluster));
      Operation prev = null;
      for (Operation op : block.getOperations()) {
        processOperation(op);
        if (prev != null)
          cfg.addEdge(prev, op);
        prev = op;
      }
      // Close the block cluster
      currentCluster = currentCluster.getParent();
    }
  }
}

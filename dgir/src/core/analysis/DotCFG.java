package core.analysis;

import core.ir.Block;
import core.ir.Operation;
import core.ir.Region;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class DotCFG {
  private DotCFG() {}

  @Contract(pure = true)
  public static @NotNull Cluster buildCfgCluster(@NotNull Operation root) {
    GraphBuilder builder = new GraphBuilder(root);
    builder.processOperation(root);
    return builder.getCfg();
  }

  /**
   * A cluster is a logical grouping of multiple operations within the CFG. It can alternatively be
   * thought of as a block, or a region. Clusters can be nested within each other to represent
   * hierarchical structures in the CFG. A cluster also contains the first operation of each child
   * cluster since it needs to draw a connection to that operation. An empty cluster represents a
   * region and can only contain other clusters.
   */
  public static class Cluster {
    private final @NotNull Operation owner;
    private final @NotNull Cluster parent;
    private final @NotNull List<Operation> operations = new ArrayList<>();
    private final @NotNull List<Cluster> children = new ArrayList<>();

    public static Function<Operation, String> identGenerator =
        op -> op.getDetails().ident().replace(".", "_") + "_" + op.hashCode();

    public Cluster(@NotNull Operation owner) {
      this.owner = owner;
      this.parent = this; // The root cluster is its own parent
    }

    public Cluster(@NotNull Operation owner, @NotNull Cluster parent) {
      this.owner = owner;
      this.parent = parent;
    }

    public @NotNull List<Operation> getOperations() {
      return operations;
    }

    public void addOperation(@NotNull Operation op) {
      operations.add(op);
    }

    public @NotNull List<Cluster> getChildren() {
      return children;
    }

    public @NotNull Cluster addChild(@NotNull Cluster child) {
      children.add(child);
      return child;
    }

    public @NotNull Operation getOwner() {
      return owner;
    }

    public @NotNull Cluster getParent() {
      return parent;
    }

    public static @NotNull String padLeftMultiline(@NotNull String s, int level) {
      return Arrays.stream(s.split("\n", -1))
          .map(line -> "\t".repeat(line.isEmpty() ? 0 : level) + line)
          .collect(Collectors.joining("\n"));
    }

    @Override
    public @NotNull String toString() {
      return toString(-1);
    }

    /**
     * Recursively creates a dot graph representation of this cluster and its children.
     *
     * @return A dot graph representation of this cluster and its children.
     */
    public @NotNull String toString(int clusterIndex) {
      // The dot graph representation of this cluster
      StringBuilder maskedDotGraph = new StringBuilder();
      maskedDotGraph
          .append(clusterIndex == -1 ? "digraph cfg" : "subgraph cluster_" + clusterIndex)
          .append(" {\n");

      StringBuilder bodyBuilder = new StringBuilder();
      if (clusterIndex != -1)
        bodyBuilder
            .append("label=\"block ")
            .append(clusterIndex)
            .append(": ")
            .append(identGenerator.apply(owner))
            .append("\";\n");
      // Handle regions
      if (!children.isEmpty()) {
        // Iterate over the regions
        for (int i = 0; i < getChildren().size(); i++) {
          StringBuilder regionBuilder = new StringBuilder();
          Cluster region = getChildren().get(i);
          regionBuilder.append("subgraph cluster_").append(i).append(" {\n");
          regionBuilder
              .append("label=\"region ")
              .append(i)
              .append(": ")
              .append(identGenerator.apply(region.getOwner()))
              .append("\";\n");
          // Iterate over all the blocks in this region and add them to the subgraph
          List<Cluster> regionChildren = region.getChildren();
          for (int j = 0; j < regionChildren.size(); j++) {
            Cluster block = regionChildren.get(j);
            // Serialize the block graph as a subgraph
            String blockGraph = block.toString(j);
            // Add the subgraph to the region subgraph with indentation
            regionBuilder.append(padLeftMultiline(blockGraph, 1));
          }
          // Close the region subgraph
          regionBuilder.append("}\n");
          // Add the region subgraph to the main graph
          bodyBuilder.append(padLeftMultiline(regionBuilder.toString(), 1));
        }
      }
      // Add the operations in this cluster to the graph
      for (Operation op : operations) {
        bodyBuilder.append(identGenerator.apply(op)).append(";\n");
      }
      // Add the connection from operation to operation
      for (int i = 0; i < operations.size() - 1; i++) {
        Operation op = operations.get(i);
        Operation nextOp = operations.get(i + 1);
        bodyBuilder
            .append(identGenerator.apply(op))
            .append(" -> ")
            .append(identGenerator.apply(nextOp))
            .append(";\n");
      }

      // Add edges from all operations in this cluster to their child regions entry block first
      // operation
      for (Operation op : operations) {
        for (Region region : op.getRegions()) {
          Operation entryOp = region.getEntryBlock().getOperations().getFirst();
          bodyBuilder
              .append(identGenerator.apply(op))
              .append(" -> ")
              .append(identGenerator.apply(entryOp))
              .append(";\n");
        }
      }

      // Add edges from all operations in the child blocks to their target blocks
      for (Cluster region : children) {
        for (Cluster block : region.getChildren()) {
          Operation lastOp = block.getOperations().getLast();
          for (Block successor : lastOp.getSuccessors()) {
            Operation entryOp = successor.getOperations().getFirst();
            bodyBuilder
                .append(identGenerator.apply(lastOp))
                .append(" -> ")
                .append(identGenerator.apply(entryOp))
                .append(";\n");
          }
        }
      }

      maskedDotGraph.append(padLeftMultiline(bodyBuilder.toString(), 1));
      maskedDotGraph.append("}\n");
      return maskedDotGraph.toString();
    }
  }

  private static class GraphBuilder {
    private final @NotNull Cluster rootCluster;

    private @NotNull Cluster currentCluster;

    GraphBuilder(@NotNull Operation root) {
      rootCluster = new Cluster(root);
      currentCluster = rootCluster;
    }

    @NotNull
    Cluster getCfg() {
      return rootCluster;
    }

    /**
     * Add this operation to the graph and all of its regions.
     *
     * @param op The operation to process
     */
    void processOperation(@NotNull Operation op) {
      currentCluster.addOperation(op);

      // If there are no regions in this operation end the generation here
      if (op.getRegions().isEmpty()) return;

      for (Region region : op.getRegions()) {
        processRegion(region);
      }
    }

    private void processRegion(@NotNull Region region) {
      // Open a new cluster for the region
      currentCluster =
          currentCluster.addChild(new Cluster(region.getParent().orElseThrow(), currentCluster));
      for (Block block : region.getBlocks()) {
        processBlock(block);
      }
      // Close the region cluster
      currentCluster = currentCluster.getParent();
    }

    private void processBlock(@NotNull Block block) {
      // Open a new cluster for the block
      currentCluster = currentCluster.addChild(new Cluster(currentCluster.owner, currentCluster));
      for (Operation op : block.getOperations()) {
        processOperation(op);
      }
      // Close the block cluster
      currentCluster = currentCluster.getParent();
    }
  }
}

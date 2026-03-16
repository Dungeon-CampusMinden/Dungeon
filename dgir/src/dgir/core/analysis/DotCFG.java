package dgir.core.analysis;

import dgir.core.ir.Block;
import dgir.core.ir.Operation;
import dgir.core.ir.Region;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds a hierarchical CFG (control-flow graph) representation in Graphviz DOT format.
 *
 * <p>The entry point is {@link #buildCfgCluster(Operation)}, which produces a {@link Cluster} tree
 * that mirrors the operation/region/block hierarchy of the IR. The resulting tree can be rendered
 * to a DOT string for visualisation with Graphviz.
 */
public class DotCFG {
  private DotCFG() {}

  /**
   * Build a {@link Cluster} tree rooted at {@code root}, covering all reachable regions and blocks.
   *
   * @param root the top-level operation to start from.
   * @return the root {@link Cluster} of the CFG tree.
   */
  @Contract(pure = true)
  public static @NotNull Cluster buildCfgCluster(@NotNull Operation root) {
    GraphBuilder builder = new GraphBuilder(root);
    builder.processOperation(root);
    return builder.getCfg();
  }

  /**
   * A node in the CFG cluster tree. A cluster represents either a region, a block, or the root
   * operation. Clusters can be nested to reflect the hierarchical structure of the IR.
   *
   * <p>The root cluster owns itself as its parent. An empty cluster (no operations) represents a
   * region and can only contain child clusters (one per block in that region).
   */
  public static class Cluster {
    /** The operation that owns this cluster (the op whose region or block this represents). */
    private final @NotNull Operation owner;

    /** The parent cluster, or {@code this} for the root. */
    private final @NotNull Cluster parent;

    /** Operations directly contained in this cluster (i.e. the block's operation list). */
    private final @NotNull List<Operation> operations = new ArrayList<>();

    /** Child clusters (one per sub-region or sub-block). */
    private final @NotNull List<Cluster> children = new ArrayList<>();

    /**
     * Function that generates a unique DOT node identifier for an operation. Defaults to {@code
     * <ident>_<hashCode>} with dots replaced by underscores.
     */
    public static Function<Operation, String> identGenerator =
        op -> op.getDetails().ident().replace(".", "_") + "_" + op.hashCode();

    /** Create a root cluster (its own parent). */
    public Cluster(@NotNull Operation owner) {
      this.owner = owner;
      this.parent = this; // The root cluster is its own parent
    }

    /** Create a child cluster with an explicit parent. */
    public Cluster(@NotNull Operation owner, @NotNull Cluster parent) {
      this.owner = owner;
      this.parent = parent;
    }

    /**
     * Returns the operations directly in this cluster.
     *
     * @return the mutable operations list.
     */
    public @NotNull List<Operation> getOperations() {
      return operations;
    }

    /**
     * Append an operation to this cluster.
     *
     * @param op the operation to add.
     */
    public void addOperation(@NotNull Operation op) {
      operations.add(op);
    }

    /**
     * Returns the child clusters of this cluster.
     *
     * @return the mutable children list.
     */
    public @NotNull List<Cluster> getChildren() {
      return children;
    }

    /**
     * Add a child cluster and return it.
     *
     * @param child the cluster to add.
     * @return {@code child}, for convenient chaining.
     */
    public @NotNull Cluster addChild(@NotNull Cluster child) {
      children.add(child);
      return child;
    }

    /**
     * Returns the operation that owns this cluster.
     *
     * @return the owner operation.
     */
    public @NotNull Operation getOwner() {
      return owner;
    }

    /**
     * Returns the parent cluster, or {@code this} if this is the root.
     *
     * @return the parent cluster.
     */
    public @NotNull Cluster getParent() {
      return parent;
    }

    /**
     * Indent each non-empty line of {@code s} by {@code level} tab characters.
     *
     * @param s the string to pad.
     * @param level the number of tab characters to prepend to each non-empty line.
     * @return the indented string.
     */
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

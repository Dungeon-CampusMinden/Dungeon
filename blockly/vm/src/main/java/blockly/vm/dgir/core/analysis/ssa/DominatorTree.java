package blockly.vm.dgir.core.analysis.ssa;

import blockly.vm.dgir.core.Block;
import blockly.vm.dgir.core.Region;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * DominatorTree is a data structure that represents the dominance relationships between blocks in a control flow graph (CFG).
 * A block A is said to dominate a block B if every path from the entry block to block B must go through block A.
 * The dominator tree is a tree where each node represents a block in the CFG, and there is an edge from node A to node B
 * if A is the dominator of B.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Dominator_(graph_theory)">Dominator Tree</a>
 */
public class DominatorTree {
  static Map<Region, DominatorTree> dominatorTrees = new HashMap<>();

  private Region region;
  // Graph representing the dominator tree (idom edges).
  private Graph<Block, DefaultEdge> graph;

  public DominatorTree() {
  }

  /**
   * Static factory method to get the dominator tree for a given {@link Region}. If the tree has already been computed and cached, it is returned from the cache.
   * Otherwise, a new tree is created, computed, cached, and returned.
   *
   * @param region The region for which to get the dominator tree.
   * @return The dominator tree for the given region.
   */
  public static DominatorTree getDominatorTree(Region region) {
    assert region.getBlocks().size() > 1 : "Can't create a dominator tree for a region with only one block.";

    // Check if this tree is already cached
    if (dominatorTrees.containsKey(region)) {
      return dominatorTrees.get(region);
    }

    // Create a new tree and cache it
    DominatorTree tree = new DominatorTree();
    tree.recalculate(region);
    dominatorTrees.put(region, tree);
    return tree;
  }

  /**
   * Recomputes the dominator tree for the given {@link Region}. The algorithm proceeds in three phases:
   * <ol>
   *   <li>Build the full CFG reachable from the entry block using a DFS walk.</li>
   *   <li>Compute the fixed-point of dominance sets (classic forward data-flow algorithm).</li>
   *   <li>Derive immediate dominators (idom) from the dominance sets and emit the dominator tree graph.</li>
   * </ol>
   * The resulting tree is stored in {@link #graph} with directed edges {@code idom -> node}.
   */
  public void recalculate(Region region) {
    assert region.getBlocks().size() > 1 : "Can't create a dominator tree for a region with only one block.";

    this.region = null;
    this.graph = null;

    this.region = region;

    // Phase 1: build CFG by walking successors from the entry block.
    Graph<Block, DefaultEdge> cfg = GraphTypeBuilder
      .<Block, DefaultEdge>directed()
      .vertexClass(Block.class)
      .edgeClass(DefaultEdge.class)
      .allowingMultipleEdges(false)
      .allowingSelfLoops(true)
      .buildGraph();

    cfg.addVertex(region.getEntryBlock());
    doDfsWalk(cfg);

    // Phase 2: iterative dominance computation. Initialize entry to {entry}, all others to universal set.
    Map<Block, Set<Block>> dominators = new HashMap<>();
    Set<Block> allBlocks = cfg.vertexSet();
    Block entry = region.getEntryBlock();
    for (Block block : allBlocks) {
      if (block.equals(entry)) {
        dominators.put(block, Set.of(entry));
      } else {
        dominators.put(block, new HashSet<>(allBlocks));
      }
    }

    boolean changed;
    do {
      changed = false;
      for (Block block : allBlocks) {
        if (block.equals(entry)) continue;

        // Intersect predecessors' dominator sets, then add the block itself per definition.
        Set<Block> newDom = new HashSet<>(allBlocks);
        for (Block pred : Graphs.predecessorListOf(cfg, block)) {
          newDom.retainAll(dominators.get(pred));
        }
        newDom.add(block);

        Set<Block> oldDom = dominators.get(block);
        if (!oldDom.equals(newDom)) {
          dominators.put(block, newDom);
          changed = true;
        }
      }
    } while (changed);

    // Phase 3: extract immediate dominators. For each block, find the nearest dominator not dominated by another candidate.
    // An immediate dominator is the dominator that is not dominated by another candidate.
    // A candidate is a dominator that is not itself.
    Graph<Block, DefaultEdge> dominatorGraph = GraphTypeBuilder
      .<Block, DefaultEdge>directed()
      .vertexClass(Block.class)
      .edgeClass(DefaultEdge.class)
      .allowingMultipleEdges(false)
      .allowingSelfLoops(false)
      .buildGraph();

    for (Block block : allBlocks) {
      dominatorGraph.addVertex(block);
    }

    for (Block block : allBlocks) {
      if (block.equals(entry)) continue;

      // Remove self, then pick the dominator that is not dominated by another candidate (nearest toward entry).
      Set<Block> doms = new HashSet<>(dominators.get(block));
      doms.remove(block);
      Block idom = null;
      for (Block candidate : doms) {
        boolean dominatedByOther = false;
        for (Block other : doms) {
          if (other.equals(candidate)) continue;
          // If another dominator also dominates candidate, candidate is higher and cannot be idom.
          if (dominators.get(candidate).contains(other)) {
            dominatedByOther = true;
            break;
          }
        }
        if (!dominatedByOther) {
          idom = candidate;
          break;
        }
      }

      assert idom != null : "Every non-entry block must have an immediate dominator.";
      dominatorGraph.addEdge(idom, block);
    }

    this.graph = dominatorGraph;
  }

  /**
   * Performs a DFS from the region entry to enumerate all reachable blocks and add CFG edges.
   * Successors discovered for the first time are pushed on the stack to continue traversal.
   */
  private void doDfsWalk(Graph<Block, DefaultEdge> cfg) {
    // A stack on which we store the last successors to work on. We start with the entry block of the region and then
    // keep adding successors to the stack as we encounter them.
    final Stack<Block> workList = new Stack<>();
    workList.push(region.getEntryBlock());

    // Do a depth first search from the entry block of the region to establish the whole cfg.
    while (!workList.isEmpty()) {
      Block currentBlock = workList.pop();
      // Add all successors of the current block to the graph and to the work list if they haven't been visited yet.
      for (Block successor : currentBlock.getSuccessors()) {
        if (!cfg.containsVertex(successor)) {
          cfg.addVertex(successor);
          workList.push(successor);
        }
        // Add an edge from the current block to the successor block
        cfg.addEdge(currentBlock, successor);
      }
    }
  }

  /**
   * Checks if the dominator block properly dominates the dominated block.
   * A block is properly dominated by another block if the dominator block dominates the dominated block and they are
   * not the same block.
   * @param dominator The dominator block.
   * @param dominated The dominated block.
   * @return {@code true} if the dominator block properly dominates the dominated block, {@code false} otherwise.
   */
  public boolean properlyDominates(Block dominator, Block dominated) {
    assert dominator != null && dominated != null : "Dominator and dominated blocks must not be null.";
    if (dominator.equals(dominated))
      return false;
    return dominates(dominator, dominated);
  }

  /**
   * Checks if the dominator block dominates the dominated block.
   * @param dominator The dominator block.
   * @param dominated The dominated block.
   * @return {@code true} if the dominator dominates the dominated block, {@code false} otherwise.
   */
  public boolean dominates(Block dominator, Block dominated) {
    assert dominator != null && dominated != null : "Dominator and dominated blocks must not be null.";
    if (dominator.equals(dominated)) return true;
    if (!isReachableFromEntry(dominator) || !isReachableFromEntry(dominated))
      return false;

    if (getImmediateDominator(dominated) == dominator) return true;
    if (getImmediateDominator(dominator) == dominated) return false;

    return dominatedByWalk(dominator, dominated);
  }

  /**
   * Checks if a block dominates another block by walking up the dominator tree. This is a very expensive operation.
   * @param dominator The dominator block.
   * @param dominated The dominated block.
   * @return {@code true} if the dominator dominates the dominated block, {@code false} otherwise.
   */
  private boolean dominatedByWalk(Block dominator, Block dominated) {
    assert dominator != null && dominated != null : "Dominator and dominated blocks must not be null.";
    assert dominator != dominated : "Dominator and dominated blocks must not be the same.";
    assert isReachableFromEntry(dominator) && isReachableFromEntry(dominated) : "Both blocks must be reachable from the entry block.";

    Block current = dominated;
    while (true) {
      Block idom = getImmediateDominator(current);
      if (idom == null) return false; // hit root without finding dominator
      if (idom.equals(dominator)) return true;
      current = idom;
    }
  }

  /**
   * Returns the immediate dominator of the given block.
   * @param block The block for which to get the immediate dominator.
   * @return The immediate dominator of the given block, or {@code null} if the block has no immediate dominator.
   */
  public Block getImmediateDominator(Block block) {
    Set<DefaultEdge> incomingEdges = graph.incomingEdgesOf(block);
    if (incomingEdges.isEmpty()) return null;
    return graph.getEdgeSource(incomingEdges.iterator().next());
  }

  /**
   * Checks whether a block is reachable from the entry block. This is done by walking up the dominator tree from the
   * given block to see if we reach the entry block.
   *
   * @param block The block to check.
   * @return {@code true} if the block is reachable from the entry block, {@code false} otherwise.
   */
  public boolean isReachableFromEntry(Block block) {
    Block current = block;
    Block entry = region.getEntryBlock();
    while (true) {
      if (current.equals(entry)) {
        return true;
      }
      Block idom = getImmediateDominator(current);
      if (idom == null)
        return false;
      current = idom;
    }
  }
}

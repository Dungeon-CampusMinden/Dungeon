package blockly.vm.dgir.core;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.HashMap;
import java.util.Map;
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
  private Graph<Block, DefaultEdge> graph;

  public DominatorTree() {
  }

  public void recalculate(Region region) {
    assert region.getBlocks().size() > 1 : "Can't create a dominator tree for a region with only one block.";

    this.region = null;
    this.graph = null;

    this.region = region;

    Graph<Block, DefaultEdge> dominatorGraph = GraphTypeBuilder
      .<Block, DefaultEdge>directed()
      .vertexClass(Block.class)
      .edgeClass(DefaultEdge.class)
      .allowingMultipleEdges(true)
      .allowingSelfLoops(true)
      .buildGraph();

    // Add the root of the dominator tree to the graph. It is always the entry block of the region.
    graph.addVertex(region.getEntryBlock());
    doDfsWalk();
  }

  // Does a depth first search from the entry block of the region to establish the whole cfg.
  // It walks from the entry node through all successor blocks and adds them to the graph, going deeper as it encounters
  // new successor blocks.
  private void doDfsWalk()
  {
    // A stack on which we store the last successors to work on. We start with the entry block of the region and then
    // keep adding successors to the stack as we encounter them.
    final Stack<Block> workList = new Stack<>();
    workList.push(region.getEntryBlock());

    // Do a depth first search from the entry block of the region to establish the whole cfg.
    while (!workList.isEmpty()) {
      Block currentBlock = workList.pop();
      // Add all successors of the current block to the graph and to the work list if they haven't been visited yet.
      for (Block successor : currentBlock.getSuccessors()) {
        if (!graph.containsVertex(successor)) {
          graph.addVertex(successor);
          workList.push(successor);
        }
        // Add an edge from the current block to the successor block
        graph.addEdge(currentBlock, successor);
      }
    }
  }

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

  public static boolean properlyDominates(Block dominator, Operation dominatorOp, Block dominated, Operation dominatedOp) {
    assert dominator != null && dominated != null : "Dominator and dominated blocks must not be null.";
    // Blocks cannot properly dominate themselves
    if (dominator.equals(dominated) && dominatorOp.equals(dominatedOp)) {
      return false;
    }

    // If the blocks are in different regions, we need to check if the dominated block has an ancestor block in the
    // dominator's region. If not, then the dominator cannot properly dominate the dominated block since only blocks
    // in the same region can have a dominance relationship.
    if (dominator.getParent() != dominated.getParent()) {
      // Find the nearest ancestor block of the dominated in the dominator's region
      var result = findAncestorInRegion(dominator.getParent(), dominated, dominatedOp);
      dominated = result.getFirst();
      dominatedOp = result.getSecond();
      if (dominated == null) {
        return false;
      }
      assert dominated.getParent() == dominator.getParent() : "The ancestor block found should belong to the dominator's region.";

      // If dominator encloses dominated, then dominated is properly dominated.
      // If the dominatorOp and dominatedOp arent the same they might not be in the correct order and we need to check if
      // the dominatorOp is defined before the dominatedOp via the dominator tree.
      // This is necessary since values can be reassigned in DGIR and therefore pure order of operations isn't information
      // enough to determine dominance.
      if (dominator == dominated && dominatorOp == dominatedOp) {
        return true;
      }
    }
    //TODO finish implementation using an actual dominator tree

    return false;
  }

  /**
   * Walks outward from the given {@code block} until it reaches the nearest ancestor block that
   * belongs to the requested {@code region}. If the block is already in that region, it is returned
   * as-is. If the block is nested under an operation whose parent region is the target, the parent
   * block of that operation is returned. If no ancestor is found in the target region, {@code null}
   * is returned.
   * <p>
   * The helper {@link Region#findAncestorOpInRegion(Operation)} is used to climb from a block’s
   * parent operation toward the requested region.</p>
   *
   * @param region    The region to search for an ancestor block.
   * @param block     The block from which to start the search.
   * @param operation The operation which is supposed to be dominated.
   * @return The nearest ancestor block of the given block that belongs to the requested region, or {@code null} if no such block exists.
   * Returns the original operation if the block is already in the target region or the op containing the region the block is nested under.
   * @see Region#findAncestorOpInRegion(Operation)
   */
  public static Pair<Block, Operation> findAncestorInRegion(Region region, Block block, Operation operation) {
    // If the block is already in the target region, return it as-is
    if (block.getParent() == region) {
      return Pair.of(block, operation);
    }

    Operation op = region.findAncestorOpInRegion(block.getParentOperation());
    if (op == null) {
      return Pair.of(null, null);
    }
    return Pair.of(op.getParent(), op);
  }
}

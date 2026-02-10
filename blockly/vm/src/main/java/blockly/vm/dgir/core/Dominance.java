package blockly.vm.dgir.core;

import org.jgrapht.alg.util.Pair;

import static blockly.vm.dgir.core.DominatorTree.getDominatorTree;

public class Dominance {
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

    return getDominatorTree(dominator.getParent()).properlyDominates(dominator, dominated);
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

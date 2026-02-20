package core.analysis;

import core.ir.*;
import core.traits.IIsolatedFromAbove;
import java.text.MessageFormat;
import java.util.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Forward must-reaching-definitions analysis for DGIR. Tracks, for each block, the set of values
 * that are definitely defined on all incoming paths.
 */
public final class ReachingDefinitions {
  /** A missing-definition diagnostic. */
  public record MissingDefinition(
      @NotNull Operation operation, @NotNull ValueOperand operand, @NotNull String message) {}

  private ReachingDefinitions() {}

  /**
   * Entry point for clients: run a must-reaching-definitions pass starting at {@code root}.
   *
   * <p>Algorithm sketch (non-SSA):
   *
   * <ul>
   *   <li>Lattice element: set of {@link Value} instances that are known to be defined on all
   *       paths.
   *   <li>Direction: forward; Meet: intersection across predecessors (must analysis).
   *   <li>Transfer: start from IN, add each op's output to the running set.
   *   <li>Nesting: for each child {@link Region}, recurse with the current state as entry facts.
   * </ul>
   *
   * The result is a list of diagnostics for operands that are used before being definitely defined.
   */
  @Contract(pure = true)
  public static @NotNull List<MissingDefinition> validate(@NotNull Operation root) {
    List<MissingDefinition> problems = new ArrayList<>();
    for (Region region : root.getRegions()) {
      // Each region starts with seeds derived from the owning operation (operands) and its own body
      // values plus parent facts if allowed.
      analyzeRegion(region, seedForRegion(root, region, Set.of()), problems);
    }
    return problems;
  }

  /**
   * Compute IN/OUT sets for all blocks in a region and flag operand uses without a reaching def.
   *
   * @param incoming seed definitions visible at the region entry (e.g., block args or enclosing
   *     defs)
   */
  private static void analyzeRegion(
      @NotNull Region region,
      @NotNull Set<Value> incoming,
      @NotNull List<MissingDefinition> problems) {
    // If the region is empty, there's nothing to analyze.
    List<Block> blocks = region.getBlocks();
    if (blocks.isEmpty()) {
      return;
    }

    // The predecessors of each block
    Map<Block, Set<Block>> preds = getPredecessors(blocks);
    // The inputs of each block
    Map<Block, Set<Value>> in = new HashMap<>();
    // The outputs of each block. These are filled as we process blocks since they can also contain
    // values defined in
    // previous blocks, such as the entry block.
    Map<Block, Set<Value>> out = new HashMap<>();

    // The list of blocks that still needs to be processed. Start with all blocks, since we don't
    // know which ones are
    // reachable until we propagate facts.
    Deque<Block> worklist = new ArrayDeque<>(blocks);
    while (!worklist.isEmpty()) {
      Block block = worklist.removeFirst();
      // Merge predecessor OUT sets to form IN, seeding entry with provided incoming facts.
      Set<Value> inSet = computeIn(block, preds, out, incoming, blocks.getFirst());
      // OUT = IN + GEN (no kills because Values are immutable identifiers here).
      Set<Value> outSet = new HashSet<>(inSet);
      outSet.addAll(gen(block));

      // If IN or OUT changed, we need to update and re-enqueue successors to see the new facts.
      // (Note: we don't need to update predecessors since they can't see the new facts anyway, and
      // the worklist will ensure we revisit successors.)
      boolean changed = !outSet.equals(out.get(block)) || !inSet.equals(in.get(block));
      if (changed) {
        // Update the sets
        in.put(block, inSet);
        out.put(block, outSet);
        // Re-enqueue successors so they can see updated facts.
        for (Block succ : block.getSuccessors()) {
          worklist.addLast(succ);
        }
      }
    }

    // Second pass: walk operations in order to check operands with intra-block precision.
    for (Block block : blocks) {
      // The IN set for this block, which is the set of values defined on all paths to the block.
      // Start with the merged IN set from predecessors.
      Set<Value> state = new HashSet<>(in.getOrDefault(block, incoming));
      // Walk operations in order, checking operands against the current state and updating the
      // state with new defs.
      for (Operation op : block.getOperations()) {
        // Validate every operand against the reaching set at this program point.
        for (ValueOperand operand : op.getOperands()) {
          if (!state.contains(
              operand
                  .getValue()
                  .orElseThrow(() -> new AssertionError("Operand value must be present")))) {
            String message =
                MessageFormat.format(
                    "Operand {0} with value {1} for operation {2}: {3} in block {4} is not defined on all paths",
                    operand.getIndex(),
                    operand.getValue().get(),
                    op.getIndex(),
                    op,
                    block.getIndex());
            problems.add(new MissingDefinition(op, operand, message));
          }
        }

        // At this point we checked all the operands and can now check the nested regions of this
        // operation.
        // This allows us to have more precise information about the reaching definitions for the
        // nested regions since
        // we will have already validated the operands of the parent operation and can use that
        // information as part of
        // the incoming facts for the nested region.

        // Recurse into child regions with the current facts as entry seeds.
        for (Region child : op.getRegions()) {
          analyzeRegion(child, seedForRegion(op, child, state), problems);
        }
        // Transfer: the op's result becomes available after the op executes.
        op.getOutputValue().ifPresent(state::add);
      }
    }
  }

  /**
   * Compute the IN set for a block: the set of values defined on all incoming paths.
   *
   * @param block The block to analyze.
   * @param preds The predecessor blocks of {@code block}.
   * @param out The OUT set for each predecessor.
   * @param entryValues The seed values for the entry block of the region, used when a block has no
   *     predecessors.
   * @param entryBlock The entry block of the region.
   * @return The IN set for the block.
   */
  @Contract(pure = true)
  private static @NotNull Set<Value> computeIn(
      @NotNull Block block,
      @NotNull Map<Block, Set<Block>> preds,
      @NotNull Map<Block, Set<Value>> out,
      @NotNull Set<Value> entryValues,
      @NotNull Block entryBlock) {
    // The predecessors of this block.
    Set<Block> predBlocks = preds.getOrDefault(block, Set.of());
    if (predBlocks.isEmpty()) {
      // Entry block gets seeded facts; unreachable blocks get an empty in-set.
      return new HashSet<>(block == entryBlock ? entryValues : Set.of());
    }

    // Must-analysis: intersect the OUT sets of all predecessors. Start with the first predecessor's
    // OUT as the initial set,
    // then retainAll for each subsequent predecessor.
    Set<Value> accumulator = null;
    for (Block pred : predBlocks) {
      // Must-analysis: start with first predecessor OUT, then intersect.
      Set<Value> outPred = out.getOrDefault(pred, Set.of());
      if (accumulator == null) {
        accumulator = new HashSet<>(outPred);
      } else {
        accumulator.retainAll(outPred);
      }
    }
    return accumulator;
  }

  /**
   * Maps each block to its predecessors.
   *
   * @param blocks The blocks to analyze.
   * @return A map from each block to its predecessors.
   */
  @Contract(pure = true)
  private static @NotNull Map<Block, Set<Block>> getPredecessors(@NotNull List<Block> blocks) {
    // Use the use list of each block to find its predecessors.
    Map<Block, Set<Block>> preds = new HashMap<>();
    for (Block block : blocks) {
      // Get the uses and get their value (block)
      preds.put(block, block.getPredecessors());
    }
    return preds;
  }

  /**
   * Compute the GEN set for a block: the set of values defined by operations in this block.
   *
   * @param block The block to analyze.
   * @return The GEN set for the block.
   */
  @Contract(pure = true)
  private static @NotNull Set<Value> gen(@NotNull Block block) {
    Set<Value> defs = new HashSet<>();
    for (Operation op : block.getOperations()) {
      op.getOutputValue().ifPresent(defs::add);
      // Body values belong to regions; they are seeded separately in seedForRegion.
    }
    return defs;
  }

  /**
   * Seed facts when entering a region of an operation: include the operation operands, the region's
   * body values, and optionally the parent state if the operation is not isolated-from-above.
   */
  @Contract(pure = true)
  private static @NotNull Set<Value> seedForRegion(
      @NotNull Operation op, @NotNull Region region, @NotNull Set<Value> parentState) {
    Set<Value> seed = new HashSet<>();
    op.getOperands()
        .forEach(
            o ->
                seed.add(
                    o.getValue()
                        .orElseThrow(() -> new AssertionError("Operand value must be present"))));
    seed.addAll(region.getBodyValues());
    if (!op.hasTrait(IIsolatedFromAbove.class)) {
      seed.addAll(parentState);
    }
    return seed;
  }
}

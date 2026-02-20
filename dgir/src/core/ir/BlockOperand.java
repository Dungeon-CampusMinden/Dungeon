package core.ir;

import org.jetbrains.annotations.NotNull;

/** A reference to a successor {@link Block} used as an operand in a branching {@link Operation}. */
public class BlockOperand extends Operand<Block, BlockOperand> {

  // =========================================================================
  // Constructors
  // =========================================================================

  public BlockOperand(@NotNull Operation owner, @NotNull Block block) {
    super(owner, block);
  }
}

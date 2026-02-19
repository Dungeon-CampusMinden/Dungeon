package core.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A reference to a successor {@link Block} used as an operand in a branching {@link Operation}.
 */
public class BlockOperand extends Operand<Block, BlockOperand> {

  // =========================================================================
  // Constructors
  // =========================================================================

  /**
   * Deserialization constructor — owner is set later by {@link Operation}.
   */
  @JsonCreator
  public BlockOperand(@JsonProperty("value") Block block) {
    super(null, block);
  }

  public BlockOperand(Operation owner, Block block) {
    super(owner, block);
  }
}

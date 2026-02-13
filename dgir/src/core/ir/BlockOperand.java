package core.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BlockOperand extends Operand<Block, BlockOperand> {
  @JsonCreator
  public BlockOperand(@JsonProperty("value") Block block) {
    super(null, block);
  }

  public BlockOperand(Operation owner, Block block) {
    super(owner, block);
  }
}

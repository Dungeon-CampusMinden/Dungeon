package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class BlockOperand extends Operand<Block> {
  @JsonIdentityReference
  private Block block;

  public BlockOperand(Block block) {
    this.block = block;
  }

  @Override
  public Block getValue() {
    return block;
  }

  @Override
  public void setValue(Block block) {
    this.block = block;
  }
}

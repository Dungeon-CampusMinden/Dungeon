package blockly.vm.dgir.core;

import java.util.ArrayList;
import java.util.List;

public final class Region {
  private final List<Block> blocks = new ArrayList<>();
  private final IOperation parent;

  public Region(IOperation parent){
    this.parent = parent;
  }

  public IOperation getParent() {
    return parent;
  }

  public Block getOrCreateDefaultBlock() {
    return blocks.stream().findFirst().orElseGet(() -> {
      Block block = new Block(this);
      blocks.add(block);
      return block;
    });
  }
}

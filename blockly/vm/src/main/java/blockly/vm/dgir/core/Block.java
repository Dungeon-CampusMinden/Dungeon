package blockly.vm.dgir.core;

import java.util.ArrayList;
import java.util.List;

public final class Block {
  private final List<IOperation> operations = new ArrayList<>();
  private final Region parent;

  public Block(Region parent){
    this.parent = parent;
  }

  Region getParent() {
    return parent;
  }
}

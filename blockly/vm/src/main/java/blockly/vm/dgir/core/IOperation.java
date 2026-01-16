package blockly.vm.dgir.core;

import blockly.vm.api.VM;
import tools.jackson.databind.JsonNode;

import java.util.Optional;

public abstract class IOperation implements Cloneable {
  private final String namespace;
  private final String name;
  private Block containingBlock;

  public IOperation(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }

  String getName() {
    return name;
  }

  String getNamespace() {
    return namespace;
  }

  String getFullName() {
    return namespace + "." + name;
  }

  Optional<Block> getContainingBlock(){
    return Optional.ofNullable(containingBlock);
  }

  public Optional<Region> getContainingRegion(){
    return getContainingBlock().map(Block::getParent);
  }

  public abstract boolean fromJson(JsonNode json, Block containingBlock);

  public abstract boolean fromString(CharSequence json, Block containingBlock);

  public abstract void run(VM.State state);

  @Override
  public IOperation clone() {
    try {
      return (IOperation) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}

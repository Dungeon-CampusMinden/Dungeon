package blockly.vm.dgir.core;

import blockly.vm.api.VM;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Optional;


@JsonPropertyOrder({ "operation" })
public abstract class IOperation implements Cloneable {
  @JsonIgnore
  private final String namespace;
  @JsonIgnore
  private final String name;

  private Block containingBlock;

  public IOperation(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public String getNamespace() {
    return namespace;
  }

  @JsonProperty("operation")
  public String getFullName() {
    return namespace + "." + name;
  }

  Optional<Block> getContainingBlock(){
    return Optional.ofNullable(containingBlock);
  }

  public Optional<Region> getContainingRegion(){
    return getContainingBlock().map(Block::getParent);
  }

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

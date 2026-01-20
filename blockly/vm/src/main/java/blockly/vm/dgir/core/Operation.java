package blockly.vm.dgir.core;

import blockly.vm.api.VM;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Optional;


@JsonPropertyOrder({"operation"})
public abstract class Operation implements Cloneable {
  @JsonIgnore
  protected IDialect _dialect;

  @JsonBackReference
  protected Block _parent;

  /**
   * The cached name of this operation.
   */
  @JsonIgnore
  private String _name = null;

  protected Operation(Class<? extends IDialect> dialectClass) {
    var dialect = DialectRegistry.get(dialectClass);
    if (dialect.isPresent()) {
      _dialect = dialect.get();
    } else {
      throw new IllegalArgumentException("Dialect not found: " + dialectClass.getName());
    }
  }

  @JsonIgnore
  public final String getName() {
    if (_name != null) {
      return _name;
    }
    _name = this.getClass().getSimpleName().replace("Op", "").toLowerCase();
    return _name;
  }

  @JsonIgnore
  public final String getNamespace() {
    return _dialect.getNamespace();
  }

  @JsonProperty("operation")
  public final String getFullName() {
    if (getNamespace().isEmpty()) {
      return getName();
    }
    return getNamespace() + "." + getName();
  }

  @JsonIgnore
  public final IDialect getDialect() {
    return _dialect;
  }

  @JsonIgnore
  public final Optional<Block> getContainingBlock() {
    return Optional.ofNullable(_parent);
  }

  public final void setContainingBlock(Block block) {
    _parent = block;
  }

  @JsonIgnore
  public Optional<Region> getContainingRegion() {
    return getContainingBlock().map(Block::getParent);
  }

  public abstract boolean fromString(CharSequence json, Block containingBlock);

  public abstract void run(VM.State state);

  @Override
  public Operation clone() {
    try {
      return (Operation) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}

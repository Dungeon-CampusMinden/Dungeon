package blockly.vm.dgir.core;

import blockly.vm.api.VM;
import blockly.vm.dgir.core.serialization.OperationTypeIdResolver;
import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@JsonPropertyOrder({"op"})
@JsonTypeInfo(
  use = JsonTypeInfo.Id.CUSTOM,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "op"
)
@JsonTypeIdResolver(OperationTypeIdResolver.class)
public abstract class Operation implements Cloneable{
  private IDialect _dialect;

  /**
   * The cached name of this operation.
   */
  private String _name = null;

  /**
   * The output of this operation.
   */
  private Optional<DynamicValue> output = Optional.empty();

  @JsonBackReference
  public Block parent;

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

  @JsonProperty("op")
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

  public final Optional<DynamicValue> getOutput() {
    return output;
  }

  protected final void setOutput(DynamicValue output) {
    this.output = Optional.ofNullable(output);
  }

  public abstract boolean fromString(CharSequence json, Block containingBlock);

  public abstract void run(VM.State state);

  // TODO check this method and if it needs to be modified
  @Override
  public Operation clone() {
    try {
      Operation clone = (Operation) super.clone();
      clone._name = this._name;
      clone._dialect = this._dialect;
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }
}

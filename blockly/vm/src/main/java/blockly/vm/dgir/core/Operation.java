package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.OperationTypeIdResolver;
import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

import java.io.Serializable;

@JsonPropertyOrder({"op"})
@JsonTypeInfo(
  use = JsonTypeInfo.Id.CUSTOM,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "op"
)
@JsonTypeIdResolver(OperationTypeIdResolver.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Operation implements Cloneable, Serializable {
  /**
   * The fully qualified name of this operation.
   */
  private final String fullName;

  /**
   * The output of this operation.
   */
  private DynamicValue output;

  /**
   * The parent block of this operation.
   */
  private Block parent;

  public Operation(Class<? extends IDialect> dialectClass, String name) {
    var dialect = DialectRegistry.getDialect(dialectClass).get();
    if (dialect.getNamespace().isEmpty())
      this.fullName = name;
    else
      this.fullName = dialect.getNamespace() + "." + name;
  }

  @JsonCreator
  public Operation(@JsonProperty("op") String fullName, @JsonProperty("output") DynamicValue output) {
    this.fullName = fullName;
    this.output = output;
  }

  @Override
  public Operation clone() {
    try {
      Operation clone = (Operation) super.clone();
      clone.output = this.output;
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  @JsonProperty("op")
  public String getFullName() {
    return fullName;
  }

  public DynamicValue getOutput() {
    return output;
  }

  public void setOutput(DynamicValue output) {
    this.output = output;
  }

  @JsonIgnore
  public Block getParent() {
    return parent;
  }

  public void setParent(Block parent) {
    this.parent = parent;
  }
}

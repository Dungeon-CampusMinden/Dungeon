package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.OperationTypeIdResolver;
import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
   * The input values of this operation.
   */
  private List<ValueOperand> operands;

  /**
   * The output of this operation.
   */
  private Value output;

  /**
   * The attributes of this operation.
   */
  private List<NamedAttribute> attributes;

  /**
   * The parent block of this operation.
   */
  private Block parent;

  public Operation(Class<? extends IDialect> dialectClass, String name) {
    fullName = Utility.getFullName(dialectClass, name);
    operands = new ArrayList<>();
    output = null;
    attributes = new ArrayList<>();
  }

  @JsonCreator
  public Operation(@JsonProperty("op") String fullName,
                   @JsonProperty("operands") List<ValueOperand> operands,
                   @JsonProperty("output") Value output,
                   @JsonProperty("attributes") List<NamedAttribute>attributes) {
    this.fullName = fullName;
    this.operands = operands;
    this.output = output;
    this.attributes = attributes;
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

  public List<ValueOperand> getOperands() {
    return operands;
  }

  public Value getOutput() {
    return output;
  }

  public void setOutput(Value output) {
    this.output = output;
  }

  public List<NamedAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<NamedAttribute> attributes) {
    this.attributes = attributes;
  }

  @JsonIgnore
  public Block getParent() {
    return parent;
  }

  public void setParent(Block parent) {
    this.parent = parent;
  }
}

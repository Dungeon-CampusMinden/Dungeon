package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.OperationTypeIdResolver;
import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

@JsonPropertyOrder({"op"})
@JsonTypeInfo(
  use = JsonTypeInfo.Id.CUSTOM,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "op"
)
@JsonTypeIdResolver(OperationTypeIdResolver.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Operation implements Cloneable {
  /**
   * The fully qualified name of this operation.
   */
  @JsonProperty("op")
  public final String fullName;

  /**
   * The output of this operation.
   */
  public DynamicValue output;

  public Operation(Class<? extends IDialect> dialectClass, String name) {
    var dialect = DialectRegistry.getDialect(dialectClass).get();
    if (dialect.getNamespace().isEmpty())
      this.fullName = name;
    else
      this.fullName = dialect.getNamespace() + "." + name;
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
}

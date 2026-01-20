package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.TypeTypeIdResolver;
import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
  use = JsonTypeInfo.Id.CUSTOM,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "type"
)
@JsonTypeIdResolver(TypeTypeIdResolver.class)
public abstract class Type {
  @JsonProperty("type")
  public final String fullName;
  public List<Type> arguments;

  public Type(Class<? extends IDialect> dialectClass, String name) {
    var dialect = DialectRegistry.getDialect(dialectClass).get();
    if (dialect.getNamespace().isEmpty())
      this.fullName = name;
    else
      this.fullName = dialect.getNamespace() + "." + name;
  }

  @JsonCreator
  public Type(@JsonProperty("type") String fullName, @JsonProperty("arguments") List<Type> arguments) {
    this.fullName = fullName;
    this.arguments = arguments;
  }

  @JsonIgnore
  public abstract Object getDefaultValue();

  public abstract boolean validate(Object value);
}

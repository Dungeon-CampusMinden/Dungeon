package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.TypeTypeIdResolver;
import com.fasterxml.jackson.annotation.*;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(
  use = JsonTypeInfo.Id.CUSTOM,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "ident"
)
@JsonTypeIdResolver(TypeTypeIdResolver.class)
public abstract class Type {
  private final String ident;
  private final List<Type> arguments;

  public Type(Class<? extends IDialect> dialectClass, String ident) {
    var dialect = DialectRegistry.getDialect(dialectClass).get();
    if (dialect.getNamespace().isEmpty())
      this.ident = ident;
    else
      this.ident = dialect.getNamespace() + "." + ident;
    this.arguments = new ArrayList<>();
  }

  @JsonCreator
  public Type(@JsonProperty("ident") String ident, @JsonProperty("arguments") List<Type> arguments) {
    this.ident = ident;
    this.arguments = arguments;
  }

  public String getIdent() {
    return ident;
  }

  public List<Type> getArguments() {
    return arguments;
  }

  @JsonIgnore
  public abstract Object getDefaultValue();

  public abstract boolean validate(Object value);
}

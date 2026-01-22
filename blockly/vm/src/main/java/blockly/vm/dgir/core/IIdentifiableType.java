package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.IdentifiableTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * An interface for types that have a type-identifier and namespace.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "ident", visible = true)
@JsonTypeIdResolver(IdentifiableTypeIdResolver.class)
@JsonPropertyOrder({"ident", "type"})
public interface IIdentifiableType {
  public String getIdent();

  @JsonIgnore
  public String getNamespace();
}

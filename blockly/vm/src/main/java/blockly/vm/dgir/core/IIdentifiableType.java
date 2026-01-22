package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * An interface for types that have a type-identifier and namespace.
 */
public interface IIdentifiableType {
  public String getIdent();

  @JsonIgnore
  public String getNamespace();
}

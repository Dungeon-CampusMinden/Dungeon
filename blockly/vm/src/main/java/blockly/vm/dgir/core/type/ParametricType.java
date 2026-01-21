package blockly.vm.dgir.core.type;

import blockly.vm.dgir.core.IDialect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Used for types that take parameters as a specification, such as bitwidth of an integer.
 * The identifier of the type takes into account the parameters, e.g. int8, int16, etc.
 */
public abstract class ParametricType extends Type {
  private final Object parameterStorage;

  public ParametricType(Class<? extends IDialect> dialectClass, String ident, Object parameters) {
    super(dialectClass, ident);
    this.parameterStorage = parameters;
  }

  @JsonIgnore
  public Object getParameters() {
    return parameterStorage;
  }

  // The ident needs to encode the parameters with it
  @Override
  public String getIdent() {
    return super.getIdent() + parameterStorage.toString();
  }

  @Override
  public boolean validate(Object value) {
    return false;
  }
}

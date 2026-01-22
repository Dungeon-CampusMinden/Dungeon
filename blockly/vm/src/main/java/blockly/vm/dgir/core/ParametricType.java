package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Used for types that take parameters as a specification, such as bitwidth of an integer.
 * The identifier of the type takes into account the parameters, e.g. int8, int16, etc.
 */
public abstract class ParametricType extends Type {
  private final Object parameterStorage;

  public ParametricType(Object parameters) {
    this.parameterStorage = parameters;
  }

  @JsonIgnore
  public Object getParameters() {
    return parameterStorage;
  }

  @Override
  public boolean validate(Object value) {
    return false;
  }
}

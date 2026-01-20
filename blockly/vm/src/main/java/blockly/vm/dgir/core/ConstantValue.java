package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.IType;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ConstantValue implements IInputValue {
  private final IType type;
  private final Object value;

  public ConstantValue(IType type, Object value) {
    this.type = type;
    if (!type.validate(value))
      throw new IllegalArgumentException("Invalid value {" + value + "} for type " + type);
    this.value = value;
  }

  @JsonIgnore
  public String getLabel() {
    return "";
  }

  public IType getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }
}

package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.IType;

public class DynamicValue implements IValue {
  private final String label;
  private final IType type;
  private Object value;

  public DynamicValue(String label, IType type, Object value) {
    this.label = label;
    this.type = type;
    if (!type.validate(value))
      throw new IllegalArgumentException("Invalid value {" + value + "} for type " + type);
    this.value = value;
  }

  public String getLabel() {
    return label;
  }

  public IType getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    if (!type.validate(value))
      throw new IllegalArgumentException("Invalid value {" + value + "} for type " + type);
    this.value = value;
  }
}

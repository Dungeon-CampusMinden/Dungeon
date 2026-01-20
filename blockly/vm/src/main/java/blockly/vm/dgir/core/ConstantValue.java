package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.IType;

public class ConstantValue implements IValue {
  private final String label;
  private final IType type;
  private final Object value;

  public ConstantValue(String label, IType type, Object value) {
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
}

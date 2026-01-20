package blockly.vm.dgir.core;

public class ConstantValue implements IInputValue {
  public Type type;
  public Object value;

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Object getValue() {
    return value;
  }
}

package blockly.vm.dgir.core;

public class ValueOperand implements IOperand<Value> {
  private final Operation owner;
  private Value value;

  public ValueOperand() {
    this.owner = null;
    this.value = null;
  }

  public ValueOperand(Operation owner, Value value) {
    this.owner = owner;
    this.value = value;
  }

  @Override
  public Operation getOwner() {
    return null;
  }

  @Override
  public Value getValue() {
    return null;
  }

  @Override
  public void setValue(Value value) {

  }
}

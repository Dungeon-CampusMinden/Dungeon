package blockly.vm.dgir.core;

/**
 * A reference to a dynamic value, which could be supplied by an operation,
 * or other source such as block arguments.
 * */
public final class ValueOperand implements IOperand<Value>, ITypeLike {
  private Operation owner;
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
    return owner;
  }

  public void setOwner(Operation owner) {
    this.owner = owner;
  }

  @Override
  public Value getValue() {
    return value;
  }

  @Override
  public void setValue(Value value) {
    this.value = value;
  }

  @Override
  public String getIdent() {
    return "value";
  }

  @Override
  public String getNamespace() {
    return "";
  }

  @Override
  public Type getType() {
    return value.getType();
  }
}

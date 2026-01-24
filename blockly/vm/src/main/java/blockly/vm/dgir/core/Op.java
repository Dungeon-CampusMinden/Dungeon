package blockly.vm.dgir.core;

public abstract class Op implements Cloneable {
  private Operation operation;

  public abstract OperationName.Impl createImpl();

  public OperationName getName() {
    return operation.getName();
  }

  // Every op should be default constructible.
  public Op() {
    this.operation = null;
  }

  // Creation of specific Op implementations is the responsibility of subclasses.
  protected Op(Operation operation) {
    this.operation = operation;
  }

  public Operation getOperation() {
    return operation;
  }

  protected void setOperation(Operation operation) {
    this.operation = operation;
  }
}

package blockly.vm.dgir.core;

/**
 * Abstract base class for all operations in the DGIR.
 * This class contains the actual state in form an operation object.
 * The derived classes are responsible for creating the specific implementations of the operation behavior.
 * They are also responsible for serialization and deserialization behavior of the operations.
 * The Op class will never be serialized, but the state will be which contains all the necessary information to recreate the operation.
 */
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

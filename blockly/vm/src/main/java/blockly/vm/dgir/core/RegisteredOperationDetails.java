package blockly.vm.dgir.core;

import java.util.Optional;

public class RegisteredOperationDetails extends OperationDetails {
  protected RegisteredOperationDetails(Impl impl) {
    super(impl);
  }

  public static void insert(Op op) {
    RegisteredOperationDetails details;
    if (op.getOperation() != null
      && op.getDetails() != null
      && op.getDetails() instanceof RegisteredOperationDetails) {
      details = (RegisteredOperationDetails) op.getDetails();
    } else {
      details = new RegisteredOperationDetails(op.createDetails());
    }

    // Register the operation name in case it doesnt exist yet
    DGIRContext.operations.put(details.getType(), details.getImpl());
    DGIRContext.operationsByIdent.put(details.getIdent(), details.getImpl());

    // Register the operation in the registry
    DGIRContext.registeredOperations.put(details.getType(), details);
    DGIRContext.registeredOperationsByIdent.put(details.getIdent(), new RegisteredOperationDetails(details.getImpl()));
  }

  public static Optional<RegisteredOperationDetails> lookup(Class<? extends Op> clazz) {
    return Optional.ofNullable(DGIRContext.registeredOperations.get(clazz));
  }

  public static Optional<RegisteredOperationDetails> lookup(String name) {
    return Optional.ofNullable(DGIRContext.registeredOperationsByIdent.get(name));
  }
}

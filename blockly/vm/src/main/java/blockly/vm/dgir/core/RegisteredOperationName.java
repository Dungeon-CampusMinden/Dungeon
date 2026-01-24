package blockly.vm.dgir.core;

import java.util.Optional;

public class RegisteredOperationName extends OperationName {
  protected RegisteredOperationName(Impl impl) {
    super(impl);
  }

  public static void insert(Impl impl) {
    // Register the operation name in case it doesnt exist yet
    DGIRContext.operationsByName.put(impl.getName(), impl);

    // Register the operation in the registry
    DGIRContext.registeredOperations.put(impl.getType(), new RegisteredOperationName(impl));
    DGIRContext.registeredOperationsByName.put(impl.getName(), new RegisteredOperationName(impl));
  }

  public static Optional<RegisteredOperationName> lookup(Class<? extends Op> clazz) {
    return Optional.ofNullable(DGIRContext.registeredOperations.get(clazz));
  }

  public static Optional<RegisteredOperationName> lookup(String name) {
    return Optional.ofNullable(DGIRContext.registeredOperationsByName.get(name));
  }
}

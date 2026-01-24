package blockly.vm.dgir.core;

import java.util.Optional;

public class RegisteredTypeName extends TypeName {
  protected RegisteredTypeName(TypeName.Impl impl) {
    super(impl);
  }

  public static void insert(TypeName.Impl impl) {
    // Register the operation name in case it doesnt exist yet
    DGIRContext.typesByName.put(impl.getName(), impl);

    // Register the operation in the registry
    DGIRContext.registeredTypes.put(impl.getType(), new RegisteredTypeName(impl));
    DGIRContext.registeredTypesByName.put(impl.getName(), new RegisteredTypeName(impl));
  }

  public static Optional<RegisteredTypeName> lookup(Class<? extends Type> clazz) {
    return Optional.ofNullable(DGIRContext.registeredTypes.get(clazz));
  }

  public static Optional<RegisteredTypeName> lookup(String name) {
    return Optional.ofNullable(DGIRContext.registeredTypesByName.get(name));
  }
}

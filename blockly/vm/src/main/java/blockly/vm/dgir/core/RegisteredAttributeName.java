package blockly.vm.dgir.core;

import java.util.Optional;

public class RegisteredAttributeName extends AttributeName {
  protected RegisteredAttributeName(AttributeName.Impl impl) {
    super(impl);
  }

  public static void insert(AttributeName.Impl impl) {
    // Register the operation name in case it doesnt exist yet
    DGIRContext.attributesByName.put(impl.getName(), impl);

    // Register the operation in the registry
    DGIRContext.registeredAttributes.put(impl.getType(), new RegisteredAttributeName(impl));
    DGIRContext.registeredAttributesByName.put(impl.getName(), new RegisteredAttributeName(impl));
  }

  public static Optional<RegisteredAttributeName> lookup(Class<? extends Attribute> clazz) {
    return Optional.ofNullable(DGIRContext.registeredAttributes.get(clazz));
  }

  public static Optional<RegisteredAttributeName> lookup(String name) {
    return Optional.ofNullable(DGIRContext.registeredAttributesByName.get(name));
  }
}

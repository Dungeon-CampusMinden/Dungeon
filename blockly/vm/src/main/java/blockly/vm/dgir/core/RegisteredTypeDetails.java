package blockly.vm.dgir.core;

import java.util.Optional;

public class RegisteredTypeDetails extends TypeDetails {
  protected RegisteredTypeDetails(TypeDetails.Impl impl) {
    super(impl);
  }

  public static void insert(Type type) {
    RegisteredTypeDetails details;
    if (type.getDetails() != null && type.getDetails() instanceof RegisteredTypeDetails) {
      details = (RegisteredTypeDetails) type.getDetails();
    }else {
      details = new RegisteredTypeDetails(type.createImpl());
    }

    // Register the operation name in case it doesnt exist yet
    DGIRContext.types.put(details.getType(), details.getImpl());
    DGIRContext.typesByIdent.put(details.getIdent(), details.getImpl());

    // Register the operation in the registry
    DGIRContext.registeredTypes.put(details.getType(), details);
    DGIRContext.registeredTypesByIdent.put(details.getIdent(), details);

    type.setDetails(details);
  }

  public static Optional<RegisteredTypeDetails> lookup(Class<? extends Type> clazz) {
    return Optional.ofNullable(DGIRContext.registeredTypes.get(clazz));
  }

  public static Optional<RegisteredTypeDetails> lookup(String name) {
    return Optional.ofNullable(DGIRContext.registeredTypesByIdent.get(name));
  }
}

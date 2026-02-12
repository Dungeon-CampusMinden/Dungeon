package blockly.vm.dgir.core.detail;

import blockly.vm.dgir.core.ir.Attribute;
import blockly.vm.dgir.core.DGIRContext;

import java.util.Optional;

public class RegisteredAttributeDetails extends AttributeDetails {
  protected RegisteredAttributeDetails(AttributeDetails.Impl impl) {
    super(impl);
  }

  public static void insert(Attribute attr) {
    RegisteredAttributeDetails details;
    if (attr.getDetails() != null && attr.getDetails() instanceof RegisteredAttributeDetails) {
      details = (RegisteredAttributeDetails) attr.getDetails();
    }else {
      details = new RegisteredAttributeDetails(attr.createImpl());
    }

    // Register the operation name in case it doesnt exist yet
    DGIRContext.attributes.put(details.getType(), details.getImpl());
    DGIRContext.attributesByIdent.put(details.getIdent(), details.getImpl());

    // Register the operation in the registry
    DGIRContext.registeredAttributes.put(details.getType(), details);
    DGIRContext.registeredAttributesByIdent.put(details.getIdent(), details);

    attr.setDetails(details);
  }

  public static Optional<RegisteredAttributeDetails> lookup(Class<? extends Attribute> clazz) {
    return Optional.ofNullable(DGIRContext.registeredAttributes.get(clazz));
  }

  public static Optional<RegisteredAttributeDetails> lookup(String name) {
    return Optional.ofNullable(DGIRContext.registeredAttributesByIdent.get(name));
  }
}

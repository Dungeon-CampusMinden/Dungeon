package blockly.vm.dgir.core.detail;

import blockly.vm.dgir.core.ir.Attribute;
import blockly.vm.dgir.core.DGIRContext;
import blockly.vm.dgir.core.Dialect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This object contains all the basic information about an attribute.
 */
public class AttributeDetails {
  public static AttributeDetails get(String ident) {
    return new AttributeDetails(ident);
  }

  public static AttributeDetails get(Class<? extends Attribute> clazz) {
    return new AttributeDetails(clazz);
  }

  protected AttributeDetails(AttributeDetails.Impl impl) {
    this.impl = impl;
  }

  public String getIdent() {
    return impl.getIdent();
  }

  @JsonIgnore
  public Class<? extends Attribute> getType() {
    return impl.getType();
  }

  @JsonIgnore
  public Dialect getDialect() {
    return impl.getDialect();
  }

  @JsonIgnore
  public AttributeDetails.Impl getImpl() {
    return impl;
  }

  private AttributeDetails.Impl impl = null;

  /**
   * This is the fully type erased interface to an attribute
   */
  public abstract static class Impl {
    protected String ident;
    protected Class<? extends Attribute> type;
    protected Dialect dialect;

    public Impl(String ident, Class<? extends Attribute> type, Dialect dialect) {
      this.ident = ident;
      this.type = type;
      this.dialect = dialect;
    }

    public String getIdent() {
      return ident;
    }

    public Class<? extends Attribute> getType() {
      return type;
    }

    public Dialect getDialect() {
      return dialect;
    }
  }

  protected final static class UnregisteredAttributeModel extends AttributeDetails.Impl {
    UnregisteredAttributeModel(String ident, Dialect dialect) {
      super(ident, Attribute.class, dialect);
    }

  }

  public AttributeDetails(String ident) {
    // Try to get the registered attribute first
    AttributeDetails registeredDetails = DGIRContext.registeredAttributesByIdent.get(ident);
    if (registeredDetails != null) {
      impl = registeredDetails.impl;
      return;
    }

    // Try to get the unregistered operation next and if that doesn't work, add a new dummy
    Impl unregisteredDetails = DGIRContext.attributesByIdent.get(ident);
    if (unregisteredDetails != null) {
      impl = unregisteredDetails;
      return;
    }

    unregisteredDetails = DGIRContext.attributesByIdent.put(ident,
      new UnregisteredAttributeModel(ident, DGIRContext.getReferencedDialect(ident)));
    DGIRContext.attributes.put(Attribute.class, unregisteredDetails);

    impl = unregisteredDetails;
  }

  public AttributeDetails(Class<? extends Attribute> clazz) {
    // Try to get the registered Type first
    AttributeDetails registeredName = DGIRContext.registeredAttributes.get(clazz);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Try to get the unregistered type next nad if that doesn't work, add a new dummy
    Impl unregisteredName = DGIRContext.attributes.get(clazz);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.attributesByIdent.put(clazz.getName(),
      new UnregisteredAttributeModel(clazz.getName(), null));
    DGIRContext.attributes.put(clazz, unregisteredName);

    impl = unregisteredName;
  }
}

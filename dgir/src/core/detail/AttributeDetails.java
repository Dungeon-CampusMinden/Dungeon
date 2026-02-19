package core.detail;

import core.ir.Attribute;
import core.DGIRContext;
import core.Dialect;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Holds all basic information about an attribute kind and exposes it through a
 * stable interface. The actual data lives in the inner {@link Impl}.
 */
public class AttributeDetails {

  // =========================================================================
  // Static Factories
  // =========================================================================

  public static AttributeDetails get(String ident) {
    return new AttributeDetails(ident);
  }

  public static AttributeDetails get(Class<? extends Attribute> clazz) {
    return new AttributeDetails(clazz);
  }

  // =========================================================================
  // Members
  // =========================================================================

  private AttributeDetails.Impl impl = null;

  // =========================================================================
  // Constructors
  // =========================================================================

  protected AttributeDetails(AttributeDetails.Impl impl) {
    this.impl = impl;
  }

  /**
   * Look up or create an {@link AttributeDetails} by ident string.
   */
  public AttributeDetails(String ident) {
    // Try the registered registry first
    AttributeDetails registeredDetails = DGIRContext.registeredAttributesByIdent.get(ident);
    if (registeredDetails != null) {
      impl = registeredDetails.impl;
      return;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
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

  /**
   * Look up or create an {@link AttributeDetails} by attribute class.
   */
  public AttributeDetails(Class<? extends Attribute> clazz) {
    // Try the registered registry first
    AttributeDetails registeredName = DGIRContext.registeredAttributes.get(clazz);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
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

  // =========================================================================
  // Delegates
  // =========================================================================

  @JsonIgnore
  public AttributeDetails.Impl getImpl() {
    return impl;
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

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public boolean equals(Object obj) {
    return obj instanceof AttributeDetails other && this.impl == other.impl;
  }

  @Override
  public int hashCode() {
    return impl.hashCode();
  }

  // =========================================================================
  // Inner: Impl
  // =========================================================================

  /**
   * Fully type-erased description of an attribute kind.
   * Subclasses are created per attribute class inside each attribute's
   * {@code createImpl()} method.
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

  // =========================================================================
  // Inner: UnregisteredAttributeModel
  // =========================================================================

  /**
   * Placeholder used when an attribute ident is referenced before registration.
   */
  protected static final class UnregisteredAttributeModel extends AttributeDetails.Impl {
    UnregisteredAttributeModel(String ident, Dialect dialect) {
      super(ident, Attribute.class, dialect);
    }
  }
}

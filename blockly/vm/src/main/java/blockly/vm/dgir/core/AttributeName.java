package blockly.vm.dgir.core;

import org.w3c.dom.Attr;

/**
 * This object contains all the basic information about an attribute.
 */
public class AttributeName {
  /**
   * This is the fully type erased interface to an attribute
   */
  public abstract static class Impl {
    protected String name;
    protected Class<? extends Attribute> type;
    protected Dialect dialect;

    public Impl(String name, Class<? extends Attribute> type, Dialect dialect) {
      this.name = name;
      this.type = type;
      this.dialect = dialect;
    }

    public String getName() {
      return name;
    }

    public Class<? extends Attribute> getType() {
      return type;
    }

    public Dialect getDialect() {
      return dialect;
    }
  }

  protected final static class UnregisteredAttribute extends AttributeName.Impl {
    UnregisteredAttribute(String name, Class<? extends Attribute> clazz, Dialect dialect) {
      super(name, clazz, dialect);
    }

  }

  protected AttributeName(AttributeName.Impl impl) {
    this.impl = impl;
  }

  public AttributeName(String name) {
    // Try to get the registered attribute first
    AttributeName registeredName = DGIRContext.registeredAttributesByName.get(name);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Try to get the unregistered operation next and if that doesn't work, add a new dummy
    AttributeName.Impl unregisteredName = DGIRContext.attributesByName.get(name);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.attributesByName.put(name,
      new AttributeName.UnregisteredAttribute(name, Attribute.class, DGIRContext.getReferencedDialect(name)));
    impl = unregisteredName;
  }

  public String getName() {
    return impl.getName();
  }

  public Class<? extends Attribute> getType() {
    return impl.getType();
  }

  public Dialect getDialect() {
    return impl.getDialect();
  }

  public AttributeName.Impl getImpl() {
    return impl;
  }

  private AttributeName.Impl impl = null;
}

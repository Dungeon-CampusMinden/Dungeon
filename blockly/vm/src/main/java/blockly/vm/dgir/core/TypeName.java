package blockly.vm.dgir.core;

/**
 * This object contains all the basic information about a type object.
 */
public class TypeName {
  /**
   * This is the fully type erased interface for a type object.
   */
  public abstract static class Impl {
    protected String name;
    protected Class<? extends Type> type;
    protected Dialect dialect;

    public Impl(String name, Class<? extends Type> type, Dialect dialect) {
      this.name = name;
      this.type = type;
      this.dialect = dialect;
    }

    public String getName() {
      return name;
    }

    public Class<? extends Type> getType() {
      return type;
    }

    public Dialect getDialect() {
      return dialect;
    }
  }

  protected final static class UnregisteredType extends blockly.vm.dgir.core.TypeName.Impl {
    UnregisteredType(String name, Class<? extends Type> clazz, Dialect dialect) {
      super(name, clazz, dialect);
    }

  }

  protected TypeName(blockly.vm.dgir.core.TypeName.Impl impl) {
    this.impl = impl;
  }

  public TypeName(String name) {
    // Try to get the registered Type first
    blockly.vm.dgir.core.TypeName registeredName = DGIRContext.registeredTypesByName.get(name);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Try to get the unregistered operation next and if that doesn't work, add a new dummy
    blockly.vm.dgir.core.TypeName.Impl unregisteredName = DGIRContext.typesByName.get(name);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.typesByName.put(name,
      new blockly.vm.dgir.core.TypeName.UnregisteredType(name, Type.class, DGIRContext.getReferencedDialect(name)));
    impl = unregisteredName;
  }

  public String getName() {
    return impl.getName();
  }

  public Class<? extends Type> getType() {
    return impl.getType();
  }

  public Dialect getDialect() {
    return impl.getDialect();
  }

  public blockly.vm.dgir.core.TypeName.Impl getImpl() {
    return impl;
  }

  private blockly.vm.dgir.core.TypeName.Impl impl = null;
}

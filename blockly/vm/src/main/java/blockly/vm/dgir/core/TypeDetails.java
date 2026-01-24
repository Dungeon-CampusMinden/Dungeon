package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This object contains all the basic information about a type object.
 */
public class TypeDetails {
  public static TypeDetails get(String ident) {
    return new TypeDetails(ident);
  }

  public static TypeDetails get(Class<? extends Type> clazz) {
    return new TypeDetails(clazz);
  }

  /**
   * This is the fully type erased interface for a type object.
   */
  public abstract static class Impl {
    protected String ident;
    protected Class<? extends Type> type;
    protected Dialect dialect;

    public Impl(String ident, Class<? extends Type> type, Dialect dialect) {
      this.ident = ident;
      this.type = type;
      this.dialect = dialect;
    }

    public String getIdent() {
      return ident;
    }

    public Class<? extends Type> getType() {
      return type;
    }

    public Dialect getDialect() {
      return dialect;
    }
  }

  protected final static class UnregisteredType extends Impl {
    UnregisteredType(String ident, Class<? extends Type> clazz, Dialect dialect) {
      super(ident, clazz, dialect);
    }

  }

  protected TypeDetails(Impl impl) {
    this.impl = impl;
  }

  public TypeDetails(String ident) {
    // Try to get the registered Type first
    TypeDetails registeredName = DGIRContext.registeredTypesByIdent.get(ident);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Try to get the unregistered operation next and if that doesn't work, add a new dummy
    Impl unregisteredName = DGIRContext.typesByIdent.get(ident);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.typesByIdent.put(ident,
      new UnregisteredType(ident, Type.class, DGIRContext.getReferencedDialect(ident)));
    DGIRContext.types.put(Type.class, unregisteredName);

    impl = unregisteredName;
  }

  public TypeDetails(Class<? extends Type> clazz) {
    // Try to get the registered Type first
    TypeDetails registeredName = DGIRContext.registeredTypes.get(clazz);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Try to get the unregistered type next nad if that doesn't work, add a new dummy
    Impl unregisteredName = DGIRContext.types.get(clazz);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.typesByIdent.put(clazz.getName(),
      new UnregisteredType(clazz.getName(), Type.class, null));
    DGIRContext.types.put(clazz, unregisteredName);

    impl = unregisteredName;
  }

  public String getIdent() {
    return impl.getIdent();
  }

  @JsonIgnore
  public Class<? extends Type> getType() {
    return impl.getType();
  }

  @JsonIgnore
  public Dialect getDialect() {
    return impl.getDialect();
  }

  @JsonIgnore
  public Impl getImpl() {
    return impl;
  }

  private Impl impl = null;
}

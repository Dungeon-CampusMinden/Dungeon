package blockly.vm.dgir.core;

import java.util.Collections;
import java.util.List;

/**
 * Holds all information about a unique operation and some of its utility methods.
 */
public class OperationName {
  /**
   * This is the fully type erased interface to an operation
   */
  public abstract static class Impl {
    protected String name;
    protected Class<? extends Op> type;
    protected Dialect dialect;
    protected List<String> attributeNames;

    public Impl(String name, Class<? extends Op> type, Dialect dialect, List<String> attributeNames) {
      this.name = name;
      this.type = type;
      this.dialect = dialect;
      this.attributeNames = Collections.unmodifiableList(attributeNames);
    }

    public String getName() {
      return name;
    }

    public Class<? extends Op> getType() {
      return type;
    }

    public Dialect getDialect() {
      return dialect;
    }

    public List<String> getAttributeNames() {
      return attributeNames;
    }

    public abstract boolean verify(Operation operation);

    public abstract void populateDefaultAttrs(List<NamedAttribute> attributes);
  }

  protected final static class UnregisteredOp extends Impl {
    UnregisteredOp(String name, Class<? extends Op> clazz, Dialect dialect, List<String> attributeNames) {
      super(name, clazz, dialect, Collections.unmodifiableList(attributeNames));
    }

    @Override
    public boolean verify(Operation operation) {
      return false;
    }

    @Override
    public void populateDefaultAttrs(List<NamedAttribute> attributes) {
    }
  }

  protected OperationName(Impl impl) {
    this.impl = impl;
  }

  public OperationName(String name) {
    // Try to get the registered operation first
    OperationName registeredName = DGIRContext.registeredOperationsByName.get(name);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Try to get the unregistered operation next and if that doesn't work, add a new dummy
    Impl unregisteredName = DGIRContext.operationsByName.get(name);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.operationsByName.put(name,
      new UnregisteredOp(name, Op.class, DGIRContext.getReferencedDialect(name), null));
    impl = unregisteredName;
  }

  public String getName() {
    return impl.getName();
  }

  public Class<? extends Op> getType() {
    return impl.getType();
  }

  public Dialect getDialect() {
    return impl.getDialect();
  }

  public List<String> getAttributeNames() {
    return impl.getAttributeNames();
  }

  public boolean verify(Operation operation) {
    return impl.verify(operation);
  }

  public void populateDefaultAttrs(List<NamedAttribute> attributes) {
    impl.populateDefaultAttrs(attributes);
  }

  public Impl getImpl() {
    return impl;
  }

  private Impl impl = null;
}

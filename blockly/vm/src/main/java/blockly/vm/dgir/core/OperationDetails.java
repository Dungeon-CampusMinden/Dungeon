package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.List;

/**
 * Holds all information about a unique operation and some of its utility methods.
 */
public class OperationDetails {
  public static OperationDetails get(String ident) {
    return new OperationDetails(ident);
  }

  public static OperationDetails get(Class<? extends Op> clazz) {
    return new OperationDetails(clazz);
  }

  /**
   * This is the fully type erased interface to an operation
   */
  public abstract static class Impl {
    protected String ident;
    protected Class<? extends Op> type;
    protected Dialect dialect;
    protected List<String> attributeNames;

    public Impl(String ident, Class<? extends Op> type, Dialect dialect, List<String> attributeNames) {
      this.ident = ident;
      this.type = type;
      this.dialect = dialect;
      this.attributeNames = Collections.unmodifiableList(attributeNames);
    }

    public String getIdent() {
      return ident;
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
    UnregisteredOp(String ident, Class<? extends Op> clazz, Dialect dialect, List<String> attributeNames) {
      super(ident, clazz, dialect, Collections.unmodifiableList(attributeNames));
    }

    @Override
    public boolean verify(Operation operation) {
      return false;
    }

    @Override
    public void populateDefaultAttrs(List<NamedAttribute> attributes) {
    }
  }

  protected OperationDetails(Impl impl) {
    this.impl = impl;
  }

  public OperationDetails(String ident) {
    // Try to get the registered operation first
    OperationDetails registeredDetails = DGIRContext.registeredOperationsByIdent.get(ident);
    if (registeredDetails != null) {
      impl = registeredDetails.impl;
      return;
    }

    // Try to get the unregistered operation next and if that doesn't work, add a new dummy
    Impl unregisteredDetails = DGIRContext.operationsByIdent.get(ident);
    if (unregisteredDetails != null) {
      impl = unregisteredDetails;
      return;
    }

    unregisteredDetails = DGIRContext.operationsByIdent.put(ident,
      new UnregisteredOp(ident, Op.class, DGIRContext.getReferencedDialect(ident), null));
    DGIRContext.operations.put(Op.class, unregisteredDetails);

    impl = unregisteredDetails;
  }

  public OperationDetails(Class<? extends Op> clazz) {
    // Try to get the registered Type first
    OperationDetails registeredName = DGIRContext.registeredOperations.get(clazz);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Try to get the unregistered type next nad if that doesn't work, add a new dummy
    Impl unregisteredName = DGIRContext.operations.get(clazz);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.operationsByIdent.put(clazz.getName(),
      new UnregisteredOp(clazz.getName(), Op.class, null, List.of()));
    DGIRContext.operations.put(clazz, unregisteredName);

    impl = unregisteredName;
  }

  public String getIdent() {
    return impl.getIdent();
  }

  @JsonIgnore
  public Class<? extends Op> getType() {
    return impl.getType();
  }

  @JsonIgnore
  public Dialect getDialect() {
    return impl.getDialect();
  }

  @JsonIgnore
  public List<String> getAttributeNames() {
    return impl.getAttributeNames();
  }

  public boolean verify(Operation operation) {
    return impl.verify(operation);
  }

  public void populateDefaultAttrs(List<NamedAttribute> attributes) {
    impl.populateDefaultAttrs(attributes);
  }

  @JsonIgnore
  public Impl getImpl() {
    return impl;
  }

  private Impl impl = null;
}

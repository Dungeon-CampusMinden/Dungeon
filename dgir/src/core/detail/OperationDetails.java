package core.detail;

import core.*;
import core.ir.NamedAttribute;
import core.ir.Op;
import core.ir.Operation;
import core.traits.IOpTrait;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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

  @JsonIgnore
  public Impl getImpl() {
    return impl;
  }

  private Impl impl = null;

  protected OperationDetails(Impl impl) {
    this.impl = impl;
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

  public Set<Class<? extends IOpTrait>> getTraits() {
    return impl.getTraits();
  }

  public boolean hasTrait(Class<? extends IOpTrait> traitClass) {
    return impl.hasTrait(traitClass);
  }

  /**
   * Create an instance of the op from the operation state.
   * Only returns a value if the operation is of type of op.
   *
   * @param clazz     The class of the op to create
   * @param operation The operation state to use
   * @return The op instance or null if the operation is not of the given type
   */
  public <T extends Op> T as(Class<T> clazz, Operation operation) {
    if (!isa(clazz)) {
      return null;
    }

    try {
      return clazz.cast(impl.operationConstructor.newInstance(operation));
    } catch (Exception e) {
      throw new RuntimeException("Failed to create operation instance of type " + clazz.getName(), e);
    }
  }

  /**
   * Create an instance of the op from the operation state.
   *
   * @param operation The operation state to use
   * @return The op instance
   */
  public Op asOp(Operation operation) {
    try {
      return impl.operationConstructor.newInstance(operation);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create operation instance of type " + getType().getName(), e);
    }
  }

  /**
   * Check if this operation is of the given type.
   *
   * @param clazz The type to check for
   * @return true if this operation is of the given type, false otherwise
   */
  public boolean isa(Class<? extends Op> clazz) {
    return clazz.equals(getType());
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof OperationDetails other && this.impl == other.impl;
  }

  @Override
  public int hashCode() {
    return impl.hashCode();
  }

  public static Optional<Constructor<? extends Op>> hasSpecificConstructor(Class<? extends Op> opClass, Class<?>... parameterTypes) {
    try {
      return Optional.of(opClass.getConstructor(Operation.class));
    } catch (NoSuchMethodException e) {
      return Optional.empty();
    }
  }

  public Optional<RegisteredOperationDetails> asRegisteredDetails() {
    if (this instanceof RegisteredOperationDetails registeredDetails) {
      return Optional.of(registeredDetails);
    }
    return Optional.empty();
  }

  public boolean verifyTraits(Operation operation) {
    Op op = asOp(operation);
    for (Class<? extends IOpTrait> trait : getTraits()) {
      Method verifier = impl.getVerifier(trait);
      try {
        boolean result = (boolean) verifier.invoke(trait.cast(op), trait.cast(op));
        if (!result) {
          operation.emitError("Operation failed verification for trait " + trait.getName());
          return false;
        }
      } catch (Exception e) {
        throw new RuntimeException("Failed to invoke verifier for trait " + trait.getName(), e);
      }
    }
    return true;
  }

  /**
   * This is the fully type erased interface to an operation
   */
  public abstract static class Impl {
    protected final String ident;
    protected final Class<? extends Op> type;
    protected final Dialect dialect;
    protected final List<String> attributeNames;
    protected final Set<Class<? extends IOpTrait>> traits;
    protected final Map<Class<? extends IOpTrait>, Method> traitVerifiers;
    protected final Constructor<? extends Op> operationConstructor;
    protected final Constructor<? extends Op> emptyConstructor;

    public Impl(String ident, Class<? extends Op> type, Dialect dialect, List<String> attributeNames) {
      this.ident = ident;
      this.type = type;
      this.dialect = dialect;
      this.attributeNames = Collections.unmodifiableList(attributeNames);
      // Only copy the Traits implemented by the Op, not the other interfaces it implements
      this.traits = Set.copyOf(
        Arrays.stream(type.getInterfaces())
          .filter(IOpTrait.class::isAssignableFrom)
          .map(aClass -> aClass.<IOpTrait>asSubclass(IOpTrait.class))
          .toList()
      );
      // Assert that all traits contain a method called 'verify' that takes an instance of the OpTrait as parameter
      // e.g. default boolean verify(IIsolatedFromAbove trait) { return true; }
      traitVerifiers = traits.stream().collect(Collectors.toMap(trait -> trait, trait -> {
        try {
          return trait.getMethod("verify", trait);
        } catch (NoSuchMethodException e) {
          throw new RuntimeException("Trait " + trait.getName() + " must have a method called verify that takes an instance of the trait as parameter.", e);
        }
      }));

      this.operationConstructor = hasSpecificConstructor(type, Operation.class).orElse(null);
      this.emptyConstructor = hasSpecificConstructor(type).orElse(null);
      assert operationConstructor != null && emptyConstructor != null
        : "Op of type " + type + " must have a constructor that takes an operation and an empty constructor.";

      //System.out.println("Created new operation details for " + ident + " with traits " + traits.stream().map(Class::getSimpleName).toList());
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

    /**
     * Called during the validation through the {@link OperationVerifier} and after the traits have been verified.
     * Therefore, any verification that depends on the traits should be implemented here, and any verification that can be
     * done independently of the traits should be implemented in the trait verifiers.
     * <p>
     * If this mehtod is reached all traits have been verified successfully.
     *
     * @param operation The operation to verify
     * @return true if the operation is valid, false otherwise
     */
    public abstract boolean verify(Operation operation);

    public abstract void populateDefaultAttrs(List<NamedAttribute> attributes);

    public boolean hasTrait(Class<? extends IOpTrait> traitClass) {
      return traits.contains(traitClass);
    }

    public Set<Class<? extends IOpTrait>> getTraits() {
      return traits;
    }

    public Map<Class<? extends IOpTrait>, Method> getTraitVerifiers() {
      return traitVerifiers;
    }

    public Method getVerifier(Class<? extends IOpTrait> traitClass) {
      return traitVerifiers.get(traitClass);
    }
  }

  protected final static class UnregisteredOp extends Impl {
    UnregisteredOp(String ident, Class<? extends Op> clazz, Dialect dialect, List<String> attributeNames) {
      super(ident, clazz, dialect, Collections.unmodifiableList(attributeNames));
      System.out.println("Created new UnregisteredOp Details with ident " + ident + " and type " + clazz.getName());
    }

    @Override
    public boolean verify(Operation operation) {
      // TODO This check still has to be implemented
      System.out.println("Missing verification for operation " + getIdent());
      return true;
    }

    @Override
    public void populateDefaultAttrs(List<NamedAttribute> attributes) {
    }
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
}

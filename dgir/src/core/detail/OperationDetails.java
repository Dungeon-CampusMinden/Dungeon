package core.detail;

import core.*;
import core.ir.NamedAttribute;
import core.ir.Op;
import core.ir.Operation;
import core.traits.IOpTrait;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds all information about an operation kind and exposes it through a
 * stable interface. The actual data lives in the inner {@link Impl}.
 */
public class OperationDetails {

  // =========================================================================
  // Static Factories
  // =========================================================================

  public static OperationDetails get(String ident) {
    return new OperationDetails(ident);
  }

  public static OperationDetails get(Class<? extends Op> clazz) {
    return new OperationDetails(clazz);
  }

  public static Optional<Constructor<? extends Op>> hasSpecificConstructor(Class<? extends Op> opClass, Class<?>... parameterTypes) {
    try {
      return Optional.of(opClass.getConstructor(Operation.class));
    } catch (NoSuchMethodException e) {
      return Optional.empty();
    }
  }

  // =========================================================================
  // Members
  // =========================================================================

  private Impl impl = null;

  // =========================================================================
  // Constructors
  // =========================================================================

  protected OperationDetails(Impl impl) {
    this.impl = impl;
  }

  /** Look up or create an {@link OperationDetails} by ident string. */
  public OperationDetails(String ident) {
    // Try the registered registry first
    OperationDetails registeredDetails = DGIRContext.registeredOperationsByIdent.get(ident);
    if (registeredDetails != null) {
      impl = registeredDetails.impl;
      return;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
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

  /** Look up or create an {@link OperationDetails} by op class. */
  public OperationDetails(Class<? extends Op> clazz) {
    // Try the registered registry first
    OperationDetails registeredName = DGIRContext.registeredOperations.get(clazz);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
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

  // =========================================================================
  // Delegates
  // =========================================================================

  @JsonIgnore
  public Impl getImpl() {
    return impl;
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

  // =========================================================================
  // Op Instantiation
  // =========================================================================

  /**
   * Wrap the given {@link Operation} in a typed {@code Op} of type {@code clazz},
   * if this details instance describes that op kind.
   *
   * @param clazz     The class of the op to create.
   * @param operation The backing operation state.
   * @return The typed op wrapper, or empty if the kinds do not match.
   */
  public <T extends Op> Optional<T> as(@NotNull Class<T> clazz, @NotNull Operation operation) {
    if (!isa(clazz)) {
      return Optional.empty();
    }
    try {
      return Optional.of(clazz.cast(impl.operationConstructor.newInstance(operation)));
    } catch (Exception e) {
      throw new RuntimeException("Failed to create operation instance of type " + clazz.getName(), e);
    }
  }

  /**
   * Wrap the given {@link Operation} in its canonical {@link Op} wrapper.
   *
   * @param operation The backing operation state.
   * @return The op wrapper.
   */
  public @NotNull Op asOp(@NotNull Operation operation) {
    try {
      return impl.operationConstructor.newInstance(operation);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create operation instance of type " + getType().getName(), e);
    }
  }

  /**
   * Check whether this operation kind matches the given class.
   *
   * @param clazz The type to check for.
   * @return {@code true} if this details instance describes {@code clazz}.
   */
  public boolean isa(@NotNull Class<? extends Op> clazz) {
    return clazz.equals(getType());
  }

  /**
   * Verify all traits registered for this operation kind against the given operation.
   * Called before the per-op {@link Impl#verify} so that trait invariants are
   * guaranteed when custom verification runs.
   *
   * @param operation The operation to verify.
   * @return {@code true} if all trait verifiers pass.
   */
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

  // =========================================================================
  // Casting
  // =========================================================================

  public Optional<RegisteredOperationDetails> asRegisteredDetails() {
    if (this instanceof RegisteredOperationDetails registeredDetails) {
      return Optional.of(registeredDetails);
    }
    return Optional.empty();
  }

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public boolean equals(Object obj) {
    return obj instanceof OperationDetails other && this.impl == other.impl;
  }

  @Override
  public int hashCode() {
    return impl.hashCode();
  }

  // =========================================================================
  // Inner: Impl
  // =========================================================================

  /**
   * Fully type-erased description of an operation kind.
   * Subclasses are created per op class inside each op's {@code createDetails()} method.
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

      // Collect only the interfaces that are IOpTrait subtypes
      this.traits = Set.copyOf(
        Arrays.stream(type.getInterfaces())
          .filter(IOpTrait.class::isAssignableFrom)
          .map(aClass -> aClass.<IOpTrait>asSubclass(IOpTrait.class))
          .toList()
      );

      // Each trait must expose a verify(TraitType) default method
      traitVerifiers = traits.stream().collect(Collectors.toMap(trait -> trait, trait -> {
        try {
          return trait.getMethod("verify", trait);
        } catch (NoSuchMethodException e) {
          throw new RuntimeException(
            "Trait " + trait.getName() + " must have a method called verify that takes an instance of the trait as parameter.", e);
        }
      }));

      this.operationConstructor = hasSpecificConstructor(type, Operation.class).orElse(null);
      this.emptyConstructor     = hasSpecificConstructor(type).orElse(null);
      assert operationConstructor != null && emptyConstructor != null
        : "Op of type " + type + " must have a constructor that takes an Operation and an empty constructor.";
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

    public Set<Class<? extends IOpTrait>> getTraits() {
      return traits;
    }

    public boolean hasTrait(Class<? extends IOpTrait> traitClass) {
      return traits.contains(traitClass);
    }

    public Map<Class<? extends IOpTrait>, Method> getTraitVerifiers() {
      return traitVerifiers;
    }

    public Method getVerifier(Class<? extends IOpTrait> traitClass) {
      return traitVerifiers.get(traitClass);
    }

    /**
     * Called during validation via {@link core.OperationVerifier}, after all trait verifiers
     * have already passed. Any verification that depends on trait guarantees belongs here.
     *
     * @param operation The operation to verify.
     * @return {@code true} if the operation is valid.
     */
    public abstract boolean verify(Operation operation);

    public abstract void populateDefaultAttrs(List<NamedAttribute> attributes);
  }

  // =========================================================================
  // Inner: UnregisteredOp
  // =========================================================================

  /** Placeholder used when an operation ident is referenced before registration. */
  protected static final class UnregisteredOp extends Impl {

    UnregisteredOp(String ident, Class<? extends Op> clazz, Dialect dialect, List<String> attributeNames) {
      super(ident, clazz, dialect, attributeNames == null ? Collections.emptyList() : Collections.unmodifiableList(attributeNames));
      System.out.println("Created new UnregisteredOp Details with ident " + ident + " and type " + clazz.getName());
    }

    @Override
    public boolean verify(Operation operation) {
      // TODO: implement proper verification
      System.out.println("Missing verification for operation " + getIdent());
      return true;
    }

    @Override
    public void populateDefaultAttrs(List<NamedAttribute> attributes) {
    }
  }
}

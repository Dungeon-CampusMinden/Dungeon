package core.detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.*;
import core.ir.NamedAttribute;
import core.ir.Op;
import core.ir.Operation;
import core.traits.IOpTrait;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds all information about an operation kind and exposes it through a stable interface. The
 * actual data lives in the inner {@link Impl}.
 */
public class OperationDetails {

  // =========================================================================
  // Static Factories
  // =========================================================================

  @Contract(pure = true)
  public static @NotNull OperationDetails get(@NotNull String ident) {
    return new OperationDetails(ident);
  }

  @Contract(pure = true)
  public static @NotNull OperationDetails get(@NotNull Class<? extends Op> clazz) {
    return new OperationDetails(clazz);
  }

  @Contract(pure = true)
  public static @NotNull Optional<Constructor<? extends Op>> hasSpecificConstructor(
      @NotNull Class<? extends Op> opClass, @NotNull Class<?>... parameterTypes) {
    try {
      return Optional.of(opClass.getDeclaredConstructor(parameterTypes));
    } catch (NoSuchMethodException e) {
      return Optional.empty();
    }
  }

  // =========================================================================
  // Members
  // =========================================================================

  private final @NotNull Impl impl;

  // =========================================================================
  // Constructors
  // =========================================================================

  protected OperationDetails(@NotNull Impl impl) {
    this.impl = impl;
  }

  /** Look up or create an {@link OperationDetails} by ident string. */
  private OperationDetails(@NotNull String ident) {
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

    unregisteredDetails =
        DGIRContext.operationsByIdent.computeIfAbsent(
            ident,
            idnt ->
                new UnregisteredOp(
                    idnt, Op.class, DGIRContext.getReferencedDialect(idnt), List.of()));
    DGIRContext.operations.put(Op.class, unregisteredDetails);
    impl = unregisteredDetails;
  }

  /** Look up or create an {@link OperationDetails} by op class. */
  private OperationDetails(@NotNull Class<? extends Op> clazz) {
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

    unregisteredName =
        DGIRContext.operationsByIdent.computeIfAbsent(
            clazz.getName(),
            idnt -> new UnregisteredOp(clazz.getName(), Op.class, null, List.of()));
    DGIRContext.operations.put(clazz, unregisteredName);
    impl = unregisteredName;
  }

  // =========================================================================
  // Delegates
  // =========================================================================

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Impl getImpl() {
    return impl;
  }

  @Contract(pure = true)
  public String getIdent() {
    return impl.getIdent();
  }

  @JsonIgnore
  @Contract(pure = true)
  public Class<? extends Op> getType() {
    return impl.getType();
  }

  @JsonIgnore
  @Contract(pure = true)
  public Dialect getDialect() {
    return impl.getDialect();
  }

  @JsonIgnore
  @Contract(pure = true)
  public List<String> getAttributeNames() {
    return impl.getAttributeNames();
  }

  @Contract(pure = true)
  public boolean verify(Operation operation) {
    return impl.verify(operation);
  }

  @Contract(pure = true)
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
   * Wrap the given {@link Operation} in a typed {@code Op} of type {@code clazz}, if this details
   * instance describes that op kind.
   *
   * @param clazz The class of the op to create.
   * @param operation The backing operation state.
   * @return The typed op wrapper, or empty if the kinds do not match.
   */
  @Contract(pure = true)
  public <T extends Op> Optional<T> as(@NotNull Class<T> clazz, @NotNull Operation operation) {
    if (!isa(clazz)) {
      return Optional.empty();
    }
    try {
      return Optional.of(clazz.cast(impl.operationConstructor.newInstance(operation)));
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to create operation instance of type " + clazz.getName(), e);
    }
  }

  /**
   * Wrap the given {@link Operation} in its canonical {@link Op} wrapper.
   *
   * @param operation The backing operation state.
   * @return The op wrapper.
   */
  @Contract(pure = true)
  public @NotNull Op asOp(@NotNull Operation operation) {
    try {
      return impl.operationConstructor.newInstance(operation);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to create operation instance of type " + getType().getName(), e);
    }
  }

  /**
   * Check whether this operation kind matches the given class.
   *
   * @param clazz The type to check for.
   * @return {@code true} if this details instance describes {@code clazz}.
   */
  @Contract(pure = true)
  public boolean isa(@NotNull Class<? extends Op> clazz) {
    return clazz.equals(getType());
  }

  /**
   * Verify all traits registered for this operation kind against the given operation. Called before
   * the per-op {@link Impl#verify} so that trait invariants are guaranteed when custom verification
   * runs.
   *
   * @param operation The operation to verify.
   * @return {@code true} if all trait verifiers pass.
   */
  @Contract(pure = true)
  public boolean verifyTraits(@NotNull Operation operation) {
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

  @Contract(pure = true)
  public @NotNull Optional<RegisteredOperationDetails> asRegisteredDetails() {
    if (this instanceof RegisteredOperationDetails registeredDetails) {
      return Optional.of(registeredDetails);
    }
    if (this.impl instanceof RegisteredOperationDetails.RegisteredOperationImpl registeredImpl) {
      return Optional.of(new RegisteredOperationDetails(registeredImpl));
    }
    return Optional.empty();
  }

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof OperationDetails other && this.impl == other.impl;
  }

  @Override
  public int hashCode() {
    return impl.hashCode();
  }

  public Op createDefaultInstance() {
    try {
      boolean accessible = impl.emptyConstructor.canAccess(null);
      if (!accessible) impl.emptyConstructor.setAccessible(true);
      Op op = impl.emptyConstructor.newInstance();
      if (!accessible) impl.emptyConstructor.setAccessible(false);
      return op;
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to create default instance of operation type "
              + getType().getName()
              + e.getMessage(),
          e);
    }
  }

  // =========================================================================
  // Inner: Impl
  // =========================================================================

  /**
   * Fully type-erased description of an operation kind. Subclasses are created per op class inside
   * each op's {@code createDetails()} method.
   */
  public abstract static class Impl {
    protected final @NotNull String ident;
    protected final @NotNull Class<? extends Op> type;
    protected final @Nullable Dialect dialect;
    protected final @NotNull List<String> attributeNames;
    protected final @NotNull Function<Operation, Boolean> verifier;
    protected final @NotNull Set<Class<? extends IOpTrait>> traits;
    protected final @NotNull Map<Class<? extends IOpTrait>, Method> traitVerifiers;
    protected final @NotNull Constructor<? extends Op> operationConstructor;
    protected final @NotNull Constructor<? extends Op> emptyConstructor;

    public Impl(@NotNull Op op) {
      this(
          op.getIdent(),
          op.getClass(),
          Dialect.getOrThrow(op.getDialect()),
          op.getDefaultAttributes().stream().map(NamedAttribute::getName).toList(),
          op.getVerifier());
    }

    public Impl(
        @NotNull String ident,
        @NotNull Class<? extends Op> type,
        @Nullable Dialect dialect,
        @NotNull List<String> attributeNames,
        @NotNull Function<Operation, Boolean> verifier) {
      this.ident = ident;
      this.type = type;
      this.dialect = dialect;
      this.attributeNames = Collections.unmodifiableList(attributeNames);
      this.verifier = verifier;

      // Collect only the interfaces that are IOpTrait subtypes
      this.traits =
          Set.copyOf(
              Arrays.stream(type.getInterfaces())
                  .filter(IOpTrait.class::isAssignableFrom)
                  .map(aClass -> aClass.<IOpTrait>asSubclass(IOpTrait.class))
                  .toList());

      // Each trait must expose a verify(TraitType) default method
      traitVerifiers =
          traits.stream()
              .collect(
                  Collectors.toMap(
                      trait -> trait,
                      trait -> {
                        try {
                          return trait.getMethod("verify", trait);
                        } catch (NoSuchMethodException e) {
                          throw new RuntimeException(
                              "Trait "
                                  + trait.getName()
                                  + " must have a method called verify that takes an instance of the trait as parameter.",
                              e);
                        }
                      }));

      this.operationConstructor =
          hasSpecificConstructor(type, Operation.class)
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "Op class "
                              + type.getName()
                              + " must have a constructor that takes an Operation."));
      this.emptyConstructor =
          hasSpecificConstructor(type)
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "Op class " + type.getName() + " must have an empty constructor."));
    }

    @Contract(pure = true)
    public @NotNull String getIdent() {
      return ident;
    }

    @Contract(pure = true)
    public @NotNull Class<? extends Op> getType() {
      return type;
    }

    @Contract(pure = true)
    public @Nullable Dialect getDialect() {
      return dialect;
    }

    @Contract(pure = true)
    public @NotNull List<String> getAttributeNames() {
      return attributeNames;
    }

    @Contract(pure = true)
    public @NotNull Set<Class<? extends IOpTrait>> getTraits() {
      return traits;
    }

    @Contract(pure = true)
    public boolean hasTrait(@NotNull Class<? extends IOpTrait> traitClass) {
      return traits.contains(traitClass);
    }

    @Contract(pure = true)
    public @NotNull Map<Class<? extends IOpTrait>, Method> getTraitVerifiers() {
      return traitVerifiers;
    }

    @Contract(pure = true)
    public Method getVerifier(@NotNull Class<? extends IOpTrait> traitClass) {
      return traitVerifiers.get(traitClass);
    }

    /**
     * Called during validation via {@link core.OperationVerifier}, after all trait verifiers have
     * already passed. Any verification that depends on trait guarantees belongs here.
     *
     * @param operation The operation to verify.
     * @return {@code true} if the operation is valid.
     */
    @Contract(pure = true)
    public final boolean verify(@NotNull Operation operation) {
      return verifier.apply(operation);
    }
  }

  // =========================================================================
  // Inner: UnregisteredOp
  // =========================================================================

  /** Placeholder used when an operation ident is referenced before registration. */
  protected static final class UnregisteredOp extends Impl {
    UnregisteredOp(
        @NotNull String ident,
        @NotNull Class<? extends Op> clazz,
        @Nullable Dialect dialect,
        @NotNull List<String> attributeNames) {
      super(
          ident,
          clazz,
          dialect,
          attributeNames,
          op -> {
            throw new RuntimeException("Operation " + ident + " has not been registered.");
          });
      System.out.println(
          "Created new UnregisteredOp Details with ident "
              + ident
              + " and type "
              + clazz.getName());
    }
  }
}

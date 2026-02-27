package core.ir;

import core.DGIRContext;
import core.Dialect;
import core.traits.IOpTrait;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Describes an operation kind and exposes its metadata through a stable interface.
 *
 * <p>Two sealed implementations exist:
 *
 * <ul>
 *   <li>{@link Registered} — fully populated once a dialect's {@link core.Dialect#init()} call
 *       invokes {@link Registered#insert(Op)} for every contributed op.
 *   <li>{@link Unregistered} — a lightweight placeholder created the first time an operation ident
 *       or class is referenced before the owning dialect has been initialised. Most accessors on
 *       this implementation throw {@link IllegalStateException}.
 * </ul>
 *
 * <p>Callers should always use the static factory methods {@link #get(String)} and {@link
 * #get(Class)} rather than constructing instances directly, so that the global {@link
 * core.DGIRContext} caches are kept consistent.
 */
public sealed interface OperationDetails {
  // =========================================================================
  // Static Factories
  // =========================================================================

  /**
   * Look up the {@link OperationDetails} for the given ident string. The registered registry is
   * checked first; if not found, the unregistered cache is consulted, and a new {@link
   * Unregistered} placeholder is created and cached if this is the first reference to that ident.
   *
   * <p><strong>Note:</strong> this method has the side effect of populating the global {@link
   * core.DGIRContext} caches when a new placeholder is created.
   *
   * @param ident the operation ident string (e.g. {@code "arith.constant"}).
   * @return the details instance, never {@code null}.
   */
  static @NotNull OperationDetails get(@NotNull String ident) {
    // Try the registered registry first
    Registered registeredDetails = DGIRContext.registeredOperationsByIdent.get(ident);
    if (registeredDetails != null) {
      return registeredDetails;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
    OperationDetails unregisteredDetails = DGIRContext.operationsByIdent.get(ident);
    if (unregisteredDetails != null) {
      return unregisteredDetails;
    }

    unregisteredDetails =
        DGIRContext.operationsByIdent.computeIfAbsent(
            ident,
            idnt -> new Unregistered(idnt, Op.class, DGIRContext.getReferencedDialect(idnt)));
    DGIRContext.operations.put(Op.class, unregisteredDetails);
    return unregisteredDetails;
  }

  /**
   * Look up the {@link OperationDetails} for the given op class. The registered registry is checked
   * first; if not found, the unregistered cache is consulted, and a new {@link Unregistered}
   * placeholder is created and cached if this is the first reference to that class.
   *
   * <p><strong>Note:</strong> this method has the side effect of populating the global {@link
   * core.DGIRContext} caches when a new placeholder is created.
   *
   * @param clazz the op class to look up.
   * @return the details instance, never {@code null}.
   */
  static @NotNull OperationDetails get(@NotNull Class<? extends Op> clazz) {
    // Try the registered registry first
    OperationDetails registeredDetails = DGIRContext.registeredOperations.get(clazz);
    if (registeredDetails != null) {
      return registeredDetails;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
    OperationDetails unregisteredDetails = DGIRContext.operations.get(clazz);
    if (unregisteredDetails != null) {
      return unregisteredDetails;
    }

    unregisteredDetails =
        DGIRContext.operationsByIdent.computeIfAbsent(
            clazz.getName(), idnt -> new Unregistered(clazz.getName(), Op.class, Optional.empty()));
    DGIRContext.operations.put(clazz, unregisteredDetails);
    return unregisteredDetails;
  }

  /**
   * Retrieve a declared constructor of {@code opClass} that matches the given parameter types.
   *
   * @param opClass the op class to inspect.
   * @param parameterTypes the exact parameter types the constructor must have.
   * @return an {@link Optional} containing the constructor, or empty if no such constructor exists.
   */
  @Contract(pure = true)
  static @NotNull Optional<Constructor<? extends Op>> getSpecificConstructor(
      @NotNull Class<? extends Op> opClass, @NotNull Class<?>... parameterTypes) {
    try {
      return Optional.of(opClass.getDeclaredConstructor(parameterTypes));
    } catch (NoSuchMethodException e) {
      return Optional.empty();
    }
  }

  // Collect interfaces from class hierarchy, including interface inheritance.
  static @NotNull Set<Class<?>> getAllInterfaces(@NotNull Class<?> clazz) {
    Set<Class<?>> interfaces = new LinkedHashSet<>();
    Class<?> current = clazz;
    while (current != null) {
      for (Class<?> iface : current.getInterfaces()) {
        collectInterfaceHierarchy(iface, interfaces);
      }
      current = current.getSuperclass();
    }
    return interfaces;
  }

  static void collectInterfaceHierarchy(@NotNull Class<?> iface, @NotNull Set<Class<?>> out) {
    if (!out.add(iface)) return;
    for (Class<?> parent : iface.getInterfaces()) {
      collectInterfaceHierarchy(parent, out);
    }
  }

  // =========================================================================
  // Delegates
  // =========================================================================

  /**
   * The unique identifier string for this operation kind (e.g. {@code "arith.constant"}).
   *
   * @return the ident string, never {@code null}.
   */
  @Contract(pure = true)
  @NotNull
  String ident();

  /**
   * The Java class that represents this operation kind.
   *
   * @return the op class, never {@code null}.
   */
  @Contract(pure = true)
  @NotNull
  Class<? extends Op> type();

  /**
   * The dialect that contributes this operation kind.
   *
   * @return the owning {@link Dialect}.
   * @throws IllegalStateException if called on an {@link Unregistered} placeholder.
   */
  @Contract(pure = true)
  @NotNull
  Dialect dialect();

  /**
   * Returns the verifier function for this operation kind. The verifier is invoked during the
   * verification phase to check that an operation instance is well-formed.
   *
   * @return the verifier function; never {@code null}.
   * @throws IllegalStateException if called on an {@link Unregistered} placeholder.
   */
  @Contract(pure = true)
  @NotNull
  Function<@NotNull Operation, Boolean> verifier();

  /**
   * Apply the verifier function to the given operation.
   *
   * @param operation the operation to verify.
   * @return {@code true} if the operation is well-formed, {@code false} otherwise.
   */
  @Contract(pure = true)
  default boolean verify(@NotNull Operation operation) {
    return verifier().apply(operation);
  }

  /**
   * The set of {@link IOpTrait} interfaces implemented by this operation kind.
   *
   * @return an unmodifiable set of trait classes; never {@code null}.
   * @throws IllegalStateException if called on an {@link Unregistered} placeholder.
   */
  @Contract(pure = true)
  @NotNull
  @Unmodifiable
  Set<Class<? extends IOpTrait>> traits();

  /**
   * A map from each registered trait class to its {@code verify} method, used during trait
   * verification.
   *
   * @return an unmodifiable map of trait verifier methods; never {@code null}.
   * @throws IllegalStateException if called on an {@link Unregistered} placeholder.
   */
  @Contract(pure = true)
  @NotNull
  @Unmodifiable
  Map<Class<? extends IOpTrait>, Method> traitVerifiers();

  /**
   * Check whether this operation kind implements the given trait.
   *
   * @param traitClass the trait class to check for.
   * @return {@code true} if the trait is present.
   */
  @Contract(pure = true)
  default boolean hasTrait(Class<? extends IOpTrait> traitClass) {
    return traits().contains(traitClass);
  }

  /**
   * The constructor that accepts a single {@link Operation} argument — used to wrap a backing
   * operation in a typed {@link Op} instance at runtime.
   *
   * @return the operation-wrapping constructor; never {@code null}.
   * @throws IllegalStateException if called on an {@link Unregistered} placeholder.
   */
  @Contract(pure = true)
  Constructor<? extends Op> operationConstructor();

  /**
   * The no-arg constructor — used to create a default op instance (e.g. during dialect
   * registration).
   *
   * @return the no-arg constructor; never {@code null}.
   * @throws IllegalStateException if called on an {@link Unregistered} placeholder.
   */
  @Contract(pure = true)
  Constructor<? extends Op> emptyConstructor();

  /**
   * Retrieve the {@code verify} method for the given trait class from the trait-verifier map.
   *
   * @param traitClass the trait whose verifier to retrieve.
   * @return the verifier {@link Method}, or {@code null} if the trait is not registered for this
   *     operation kind.
   */
  @Contract(pure = true)
  default @NotNull Optional<Method> getTraitVerifier(Class<? extends IOpTrait> traitClass) {
    return Optional.ofNullable(traitVerifiers().get(traitClass));
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
  default <T extends Op> Optional<T> as(@NotNull Class<T> clazz, @NotNull Operation operation) {
    if (!isa(clazz)) {
      return Optional.empty();
    }
    try {
      return Optional.of(clazz.cast(operationConstructor().newInstance(operation)));
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
  default @NotNull Op asOp(@NotNull Operation operation) {
    try {
      return operationConstructor().newInstance(operation);
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to create operation instance of type " + type().getName(), e);
    }
  }

  /**
   * Check whether this operation kind matches the given class.
   *
   * @param clazz The type to check for.
   * @return {@code true} if this details instance describes {@code clazz}.
   */
  @Contract(pure = true)
  default boolean isa(@NotNull Class<? extends Op> clazz) {
    return clazz.equals(type());
  }

  /**
   * Verify all traits registered for this operation kind against the given operation. Called before
   * the per-op {@link #verify} so that trait invariants are guaranteed when custom verification
   * runs.
   *
   * @param operation The operation to verify.
   * @return {@code true} if all trait verifiers pass.
   */
  @Contract(pure = true)
  default boolean verifyTraits(@NotNull Operation operation) {
    Op op = asOp(operation);
    for (Class<? extends IOpTrait> trait : traits()) {
      Method verifier =
          getTraitVerifier(trait)
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "No verifier found for trait " + trait.getName() + " on op " + ident()));
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
  // Op Construction
  // =========================================================================

  /**
   * Create a default (no-arg) instance of the op represented by this details object. Intended for
   * use during dialect registration and introspection, not for building live IR nodes.
   *
   * <p>If the no-arg constructor is not publicly accessible it is temporarily made accessible for
   * the duration of the call and restored afterwards.
   *
   * @return a freshly constructed default op instance; never {@code null}.
   * @throws RuntimeException if the no-arg constructor cannot be invoked.
   */
  default Op createDefaultInstance() {
    try {
      boolean accessible = emptyConstructor().canAccess(null);
      if (!accessible) emptyConstructor().setAccessible(true);
      Op op = emptyConstructor().newInstance();
      if (!accessible) emptyConstructor().setAccessible(false);
      return op;
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to create default instance of operation type "
              + type().getName()
              + e.getMessage(),
          e);
    }
  }

  // =========================================================================
  // Registered
  // =========================================================================

  /**
   * Fully populated description of an operation kind. Instances are created via {@link #create(Op)}
   * and registered into the global {@link core.DGIRContext} caches by {@link #insert(Op)}, which is
   * called for every op contributed by a dialect during {@link core.Dialect#init()}.
   */
  record Registered(
      @NotNull String ident,
      @NotNull Class<? extends Op> type,
      @NotNull Dialect dialect,
      @NotNull List<String> attributeNames,
      @NotNull Function<Operation, Boolean> verifier,
      @NotNull Set<Class<? extends IOpTrait>> traits,
      @NotNull Map<Class<? extends IOpTrait>, Method> traitVerifiers,
      @NotNull Constructor<? extends Op> operationConstructor,
      @NotNull Constructor<? extends Op> emptyConstructor)
      implements OperationDetails {
    /**
     * Build a {@link Registered} instance from a default (no-arg) {@link Op} prototype. All fields
     * are derived by introspecting the op's class and the values returned by its abstract methods.
     *
     * <p>The owning dialect must already be registered in {@link core.DGIRContext} before this
     * method is called, because {@link core.Dialect#getOrThrow(Class)} is used to resolve it.
     *
     * @param op a default (no-arg) op prototype; must not be {@code null}.
     * @return a fully populated {@link Registered} instance.
     * @throws RuntimeException if the op class is missing required constructors or any registered
     *     {@link IOpTrait} does not expose the expected {@code verify} method.
     */
    public static @NotNull Registered create(@NotNull Op op) {
      final var ident = op.getIdent();
      final var type = op.getClass();
      final var dialect = Dialect.getOrThrow(op.getDialect());
      final var attributeNames =
          op.getDefaultAttributes().stream().map(NamedAttribute::getName).toList();
      final var verifier = op.getVerifier();
      final Set<Class<? extends IOpTrait>> traits =
          Set.copyOf(
              OperationDetails.getAllInterfaces(type).stream()
                  .filter(IOpTrait.class::isAssignableFrom)
                  .filter(aClass -> !aClass.equals(IOpTrait.class))
                  .map(aClass -> aClass.<IOpTrait>asSubclass(IOpTrait.class))
                  .toList());
      final Map<Class<? extends IOpTrait>, Method> traitVerifiers =
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

      final var operationConstructor =
          getSpecificConstructor(type, Operation.class)
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "Op class "
                              + type.getName()
                              + " must have a constructor that takes an Operation."));
      final var emptyConstructor =
          getSpecificConstructor(type)
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "Op class " + type.getName() + " must have an empty constructor."));

      return new Registered(
          ident,
          type,
          dialect,
          attributeNames,
          verifier,
          traits,
          traitVerifiers,
          operationConstructor,
          emptyConstructor);
    }

    // =========================================================================
    // Static Registration
    // =========================================================================

    /**
     * Register the given op prototype into the global {@link core.DGIRContext} caches. If the op
     * already carries a {@link Registered} details instance (i.e. it was previously registered),
     * that instance is reused; otherwise {@link #create(Op)} is called first.
     *
     * <p>This method populates both the unregistered caches (so look-ups that arrive before full
     * dialect initialisation still resolve) and the registered caches (used for all post-init
     * look-ups).
     *
     * @param op the op prototype to register; must not be {@code null}.
     */
    public static void insert(@NotNull Op op) {
      Registered details;
      if (op.getOperationOrNull() != null && op.getDetails() instanceof Registered existing) {
        details = existing;
      } else {
        details = create(op);
      }

      // Populate the unregistered caches so look-ups before registration still resolve
      DGIRContext.operations.put(details.type(), details);
      DGIRContext.operationsByIdent.put(details.ident(), details);

      // Populate the registered caches
      DGIRContext.registeredOperations.put(details.type(), details);
      DGIRContext.registeredOperationsByIdent.put(details.ident(), details);
    }

    // =========================================================================
    // Static Lookups
    // =========================================================================

    /**
     * Look up a {@link Registered} entry by op class.
     *
     * @param clazz the op class to look up.
     * @return the registered details, or empty if the class has not been registered yet.
     */
    @Contract(pure = true)
    public static @NotNull Optional<Registered> lookup(@NotNull Class<? extends Op> clazz) {
      return Optional.ofNullable(DGIRContext.registeredOperations.get(clazz));
    }

    /**
     * Look up a {@link Registered} entry by operation ident string.
     *
     * @param name the ident string (e.g. {@code "arith.constant"}) to look up.
     * @return the registered details, or empty if the ident has not been registered yet.
     */
    @Contract(pure = true)
    public static @NotNull Optional<Registered> lookup(@NotNull String name) {
      return Optional.ofNullable(DGIRContext.registeredOperationsByIdent.get(name));
    }
  }

  // =========================================================================
  // Inner: UnregisteredOp
  // =========================================================================

  /**
   * Placeholder created the first time an operation ident or class is referenced before its owning
   * dialect has been initialised.
   *
   * <p>The {@code type} field is always set to {@link core.ir.Op Op.class} because the concrete
   * subclass is not yet known. The {@code dialectOpt} field is populated by {@link
   * core.DGIRContext#getReferencedDialect(String)} when the ident contains a namespace prefix, and
   * left empty when the placeholder is created from a bare class name.
   *
   * <p>Most accessors throw {@link IllegalStateException}; only {@link #ident()}, {@link #type()},
   * and {@link #dialect()} (when a dialect could be resolved) are usable on this placeholder.
   */
  record Unregistered(
      @NotNull String ident,
      @NotNull Class<? extends Op> type,
      @NotNull Optional<Dialect> dialectOpt)
      implements OperationDetails {

    @Override
    public @NotNull Dialect dialect() {
      if (dialectOpt.isPresent()) return dialectOpt.get();
      throw new IllegalStateException("Cannot get dialect for unregistered op: " + ident);
    }

    @Override
    public @NotNull Function<@NotNull Operation, Boolean> verifier() {
      throw new IllegalStateException("Cannot get verifier for unregistered op: " + ident);
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends IOpTrait>> traits() {
      throw new IllegalStateException("Cannot get traits for unregistered op: " + ident);
    }

    @Override
    public @NotNull @Unmodifiable Map<Class<? extends IOpTrait>, Method> traitVerifiers() {
      throw new IllegalStateException("Cannot get trait verifiers for unregistered op: " + ident);
    }

    @Override
    public Constructor<? extends Op> operationConstructor() {
      throw new IllegalStateException(
          "Cannot get operation constructor for unregistered op: " + ident);
    }

    @Override
    public Constructor<? extends Op> emptyConstructor() {
      throw new IllegalStateException("Cannot get empty constructor for unregistered op: " + ident);
    }
  }
}

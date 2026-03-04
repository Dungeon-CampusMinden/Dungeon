package dgir.core.ir;

import dgir.core.DGIRContext;
import dgir.core.Dialect;
import dgir.core.Utils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds all basic information about a type kind and exposes it through a stable interface. The
 * actual data lives in the inner {@link Registered} record.
 *
 * <p>Two implementations exist:
 *
 * <ul>
 *   <li>{@link Registered} — fully populated after a dialect's {@link Dialect#register()} call.
 *   <li>{@link Unregistered} — placeholder inserted the first time a type ident is referenced
 *       before the owning dialect has been initialised.
 * </ul>
 *
 * <p>Callers should always use the static factory methods {@link #get(String)} and {@link
 * #get(Class)} rather than constructing instances directly, so that the global {@link DGIRContext}
 * caches are kept consistent.
 */
public sealed interface TypeDetails {
  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * The unique identifier string for this type (e.g. {@code "int"} or {@code "func.func"}).
   *
   * @return the ident string, never {@code null}.
   */
  @Contract(pure = true)
  @NotNull
  String ident();

  /**
   * The Java class that represents this type.
   *
   * @return the type class, never {@code null}.
   */
  @Contract(pure = true)
  @NotNull
  Class<? extends Type> type();

  /**
   * The dialect that contributes this type.
   *
   * @return the owning {@link Dialect}.
   * @throws IllegalStateException if called on an {@link Unregistered} placeholder.
   */
  @Contract(pure = true)
  @NotNull
  Dialect dialect();

  /**
   * Creates and returns a fresh default instance of this type using its no-arg constructor.
   *
   * @return a new default type instance.
   * @throws IllegalStateException if called on an {@link Unregistered} placeholder.
   */
  @Contract(pure = true)
  @NotNull
  Type defaultInstance();

  /**
   * Returns the validator function that checks whether a given value is compatible with this type.
   *
   * @return a validator {@link Function}; for unregistered types the validator emits a warning and
   *         returns {@code true}.
   */
  @Contract(pure = true)
  @NotNull
  Function<Object, Boolean> validator();

  // =========================================================================
  // Static Factories
  // =========================================================================

  /**
   * Look up the {@link TypeDetails} for the given ident string. The registered registry is checked
   * first; if not found, the unregistered cache is consulted, and a new {@link Unregistered}
   * placeholder is created and cached if this is the first reference to that ident.
   *
   * <p><strong>Note:</strong> this method has the side effect of populating the global {@link
   * DGIRContext} caches when a new placeholder is created.
   *
   * @param ident the type ident string (e.g. {@code "int32"} or {@code "func.func"}).
   * @return the details instance, never {@code null}.
   */
  static @NotNull TypeDetails get(@NotNull String ident) {
    // Try the registered registry first
    Registered registeredDetails = DGIRContext.registeredTypesByIdent.get(ident);
    if (registeredDetails != null) {
      return registeredDetails;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
    TypeDetails unregisteredDetails = DGIRContext.typesByIdent.get(ident);
    if (unregisteredDetails != null) {
      return unregisteredDetails;
    }

    unregisteredDetails =
        DGIRContext.typesByIdent.computeIfAbsent(
            ident,
            idnt -> new Unregistered(idnt, Type.class, DGIRContext.getReferencedDialect(idnt)));
    DGIRContext.types.put(Type.class, unregisteredDetails);
    return unregisteredDetails;
  }

  /**
   * Look up the {@link TypeDetails} for the given type class. The registered registry is checked
   * first; if not found, the unregistered cache is consulted, and a new {@link Unregistered}
   * placeholder is created and cached if this is the first reference to that class.
   *
   * <p><strong>Note:</strong> this method has the side effect of populating the global {@link
   * DGIRContext} caches when a new placeholder is created.
   *
   * @param clazz the type class to look up.
   * @return the details instance, never {@code null}.
   */
  static @NotNull TypeDetails get(@NotNull Class<? extends Type> clazz) {
    // Try the registered registry first
    TypeDetails registeredName = DGIRContext.registeredTypes.get(clazz);
    if (registeredName != null) {
      return registeredName;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
    TypeDetails unregisteredName = DGIRContext.types.get(clazz);
    if (unregisteredName != null) {
      return unregisteredName;
    }

    unregisteredName =
        DGIRContext.typesByIdent.computeIfAbsent(
            clazz.getName(),
            ident -> new Unregistered(clazz.getName(), Type.class, Optional.empty()));
    DGIRContext.types.put(clazz, unregisteredName);
    return unregisteredName;
  }

  // =========================================================================
  // Static Helpers
  // =========================================================================

  /**
   * Create a Type instance from the provided parameterized ident. Works for both simple and
   * generic/complex types (e.g. {@code func.func<...>}).
   *
   * <p>Examples:
   *
   * <pre>{@literal
   *   int32
   *   float64
   *   func.func<(int32, string) -> (bool)>
   *   func.func<(func.func<(int32) -> (bool)>) -> ()>
   * }</pre>
   *
   * @param parameterizedIdent The parameterized ident string.
   * @return The created Type instance.
   */
  @Contract(pure = true)
  static @NotNull Type fromParameterizedIdent(@NotNull String parameterizedIdent) {
    String baseIdent = parameterizedIdent;
    int genericStart = parameterizedIdent.indexOf('<');
    if (genericStart != -1) {
      baseIdent = parameterizedIdent.substring(0, genericStart);
    }
    return switch (TypeDetails.get(baseIdent)) {
      case Unregistered ignored -> throw new IllegalArgumentException(
          "Cannot create type from parameterized ident for unregistered type: "
              + parameterizedIdent);
      case Registered registered -> registered.parameterizedIdentFactory.apply(Pair.of(parameterizedIdent, registered));
    };
  }

  /**
   * Parse a comma-separated list of (possibly nested/parameterized) type strings into a list of
   * Type instances.
   *
   * <p>Splitting is performed by {@link Utils#splitAtDepthZero(String, String)} so that commas
   * inside nested angle-bracket ({@code < >}) or parenthesis ({@code ( )}) groups are never
   * treated as separators.
   *
   * <p>Examples:
   *
   * <pre>{@literal
   *   int32, float64
   *   func.func<(int32, string) -> (bool)>, float64
   *   func.func<(func.func<(int32) -> (bool)>) -> ()>, string
   * }</pre>
   *
   * @param parameterString The comma-separated parameter string (may be empty).
   * @return The list of created Type instances; empty if {@code parameterString} is blank.
   */
  @Contract(pure = true)
  static @NotNull List<Type> fromParameterString(@NotNull String parameterString) {
    if (parameterString.isBlank()) {
      return List.of();
    }
    return Utils.splitAtDepthZero(parameterString, ",").stream()
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(TypeDetails::fromParameterizedIdent)
        .toList();
  }

  /**
   * Retrieve the no-arg constructor of the given type class.
   *
   * @param clazz the type class to inspect.
   * @return the declared no-arg {@link Constructor}.
   * @throws RuntimeException if the class does not declare a no-arg constructor.
   */
  static Constructor<? extends Type> getDefaultConstructor(Class<? extends Type> clazz) {
    try {
      return clazz.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(
          "Type class must have a default constructor: " + clazz.getSimpleName(), e);
    }
  }

  // =========================================================================
  // Registered
  // =========================================================================

  /**
   * A fully registered type, with all information available. Created by {@link #insert(Type)}
   * during dialect initialization.
   *
   * <p>Do not create instances of this class directly as that would go against the design of the
   * type registry. Always use the {@link #insert(Type)} method to ensure that the type is properly
   * registered and all caches are populated.
   *
   * @param defaultInstance              a pre-built default instance of the type (may be
   *                                     {@code null} for parameterized types that have no natural
   *                                     default).
   * @param ident                        the unique ident string (e.g. {@code "int"}).
   * @param type                         the Java class of the type.
   * @param dialect                      the dialect that contributed this type.
   * @param constructor                  the no-arg constructor used to create new instances.
   * @param validator                    value validator function.
   * @param parameterizedIdentFactory    factory that reconstructs a typed instance from its
   *                                     parameterized ident string.
   */
  record Registered(
      @Nullable Type defaultInstance,
      @NotNull String ident,
      @NotNull Class<? extends Type> type,
      @Nullable Dialect dialect,
      @NotNull Constructor<? extends Type> constructor,
      @NotNull Function<Object, Boolean> validator,
      Function<Pair<String, TypeDetails>, Type> parameterizedIdentFactory)
      implements TypeDetails {

    /**
     * Build a {@link Registered} from a live type instance. All fields are derived from the
     * type's own methods.
     *
     * @param type the type prototype to register.
     */
    private Registered(@NotNull Type type) {
      this(
          type,
          type.getIdent(),
          type.getClass(),
          Dialect.getOrThrow(type.getDialect()),
          getDefaultConstructor(type.getClass()),
          type.getValidator(),
          type.getParameterizedStringFactory());
    }

    /**
     * Create a new instance of this type and cast it to {@code clazz}.
     *
     * @param <T>   the desired type.
     * @param clazz the class to cast to.
     * @return an {@link Optional} containing the new instance, or empty if the cast fails.
     */
    @Contract(pure = true)
    public <T extends Type> Optional<T> createInstance(@NotNull Class<T> clazz) {
      try {
        boolean accessible = constructor.canAccess(null);
        if (!accessible) constructor.setAccessible(true);
        Type type = constructor.newInstance();
        if (!accessible) constructor.setAccessible(false);
        if (!clazz.isInstance(type)) return Optional.empty();
        return Optional.of(clazz.cast(type));
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException("Failed to create instance of type: " + type.getSimpleName(), e);
      }
    }

    // =========================================================================
    // Static Registration
    // =========================================================================

    /**
     * Register the given type in the global {@link DGIRContext}. This should only be called from a
     * dialect's {@code init()} method during dialect initialization. This will populate both the
     * unregistered and registered caches in the {@link DGIRContext} to ensure that look-ups work
     * both before and after registration.
     *
     * @param type The type instance to register.
     */
    public static void insert(@NotNull Type type) {
      Registered details;
      if (type.getDetails() instanceof Registered existing) {
        details = existing;
      } else {
        details = new Registered(type);
      }

      // Populate the unregistered caches so look-ups before registration still resolve
      DGIRContext.types.put(details.type(), details);
      DGIRContext.typesByIdent.put(details.ident(), details);

      // Populate the registered caches
      DGIRContext.registeredTypes.put(details.type(), details);
      DGIRContext.registeredTypesByIdent.put(details.ident(), details);

      type.setDetails(details);
    }

    // =========================================================================
    // Static Lookups
    // =========================================================================

    /**
     * Look up a registered type by class.
     *
     * @param clazz the type class to look up.
     * @return an optional containing the registered details, or empty if not yet registered.
     */
    @Contract(pure = true)
    static @NotNull Optional<Registered> lookup(@NotNull Class<? extends Type> clazz) {
      return Optional.ofNullable(DGIRContext.registeredTypes.get(clazz));
    }

    /**
     * Look up a registered type by ident string.
     *
     * @param name the ident string to look up (e.g. {@code "int"} or {@code "func.func"}).
     * @return an optional containing the registered details, or empty if not yet registered.
     */
    @Contract(pure = true)
    static @NotNull Optional<Registered> lookup(@NotNull String name) {
      return Optional.ofNullable(DGIRContext.registeredTypesByIdent.get(name));
    }
  }

  // =========================================================================
  // Unregistered
  // =========================================================================

  /**
   * Placeholder created the first time a type ident or class is referenced before its owning
   * dialect has been initialised.
   *
   * <p>The {@code type} field is always set to {@link Type Type.class} because the concrete
   * subclass is not yet known. The {@code dialectOpt} field is populated by {@link
   * DGIRContext#getReferencedDialect(String)} when the ident contains a namespace prefix, and left
   * empty when the placeholder is created from a bare class name.
   *
   * <p>Only {@link #ident()}, {@link #type()}, and {@link #dialect()} (when a dialect could be
   * resolved) are usable on this placeholder; {@link #defaultInstance()} throws {@link
   * IllegalStateException}, and {@link #validator()} returns a permissive fallback.
   *
   * @param ident the ident string that was referenced.
   * @param type always {@link Type}{@code .class} for unregistered entries.
   * @param dialectOpt the owning dialect if it could be resolved from the ident prefix, otherwise
   *     empty.
   */
  record Unregistered(
      @NotNull String ident,
      @NotNull Class<? extends Type> type,
      @NotNull Optional<Dialect> dialectOpt)
      implements TypeDetails {

    @Override
    public @NotNull Dialect dialect() {
      if (dialectOpt.isPresent()) return dialectOpt.get();
      throw new IllegalStateException("Cannot get dialect for unregistered type: " + ident);
    }

    @Override
    public @NotNull Type defaultInstance() {
      throw new IllegalStateException(
          "Cannot get default instance for unregistered type: " + ident);
    }

    @Override
    public @NotNull Function<Object, Boolean> validator() {
      return value -> {
        System.err.println(
            "WARNING: Value " + value + " cannot be validated for unregistered type: " + ident);
        return true;
      };
    }
  }
}

package core.detail;

import core.DGIRContext;
import core.Dialect;
import core.ir.Type;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds all basic information about a type kind and exposes it through a
 * stable interface. The actual data lives in the inner {@link Impl}.
 */
public class TypeDetails {

  // =========================================================================
  // Static Factories
  // =========================================================================

  public static @NotNull TypeDetails get(@NotNull String ident) {
    return new TypeDetails(ident);
  }

  public static @NotNull TypeDetails get(@NotNull Class<? extends Type> clazz) {
    return new TypeDetails(clazz);
  }

  // =========================================================================
  // Members
  // =========================================================================

  private final @NotNull Impl impl;

  // =========================================================================
  // Constructors
  // =========================================================================

  protected TypeDetails(@NotNull Impl impl) {
    this.impl = impl;
  }

  /**
   * Look up or create a {@link TypeDetails} by ident string.
   */
  public TypeDetails(@NotNull String ident) {
    // Try the registered registry first
    TypeDetails registeredName = DGIRContext.registeredTypesByIdent.get(ident);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
    Impl unregisteredName = DGIRContext.typesByIdent.get(ident);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.typesByIdent.computeIfAbsent(ident,
      idnt -> new UnregisteredType(idnt, Type.class, DGIRContext.getReferencedDialect(idnt)));
    DGIRContext.types.put(Type.class, unregisteredName);
    impl = Objects.requireNonNull(unregisteredName);
  }

  /**
   * Look up or create a {@link TypeDetails} by type class.
   */
  public TypeDetails(@NotNull Class<? extends Type> clazz) {
    // Try the registered registry first
    TypeDetails registeredName = DGIRContext.registeredTypes.get(clazz);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
    Impl unregisteredName = DGIRContext.types.get(clazz);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName = DGIRContext.typesByIdent.computeIfAbsent(clazz.getName(),
      ident -> new UnregisteredType(clazz.getName(), Type.class, null));
    DGIRContext.types.put(clazz, unregisteredName);
    impl = unregisteredName;
  }

  // =========================================================================
  // Delegates
  // =========================================================================

  @JsonIgnore
  public @NotNull Impl getImpl() {
    return impl;
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
  public String getParameterizedIdent(Type type) {
    return impl.getParameterizedIdent(type);
  }

  // =========================================================================
  // Static Helpers
  // =========================================================================

  /**
   * Create a Type instance from the provided parameterized ident.
   * Works for both simple and generic/complex types (e.g. {@code func.func<...>}).
   * <p>Examples:
   * <pre>{@literal
   *   int32
   *   float64
   *   func.func<((int, string) -> (bool))>
   * }</pre>
   *
   * @param parameterizedIdent The parameterized ident string.
   * @return The created Type instance.
   */
  public static Type fromParameterizedIdent(String parameterizedIdent) {
    String baseIdent = parameterizedIdent;
    int genericStart = parameterizedIdent.indexOf('<');
    if (genericStart != -1) {
      baseIdent = parameterizedIdent.substring(0, genericStart);
    }
    return TypeDetails.get(baseIdent).getImpl().fromParameterizedIdent(parameterizedIdent);
  }

  /**
   * Parse a comma-separated list of (possibly nested/parameterized) type strings
   * into a list of Type instances.
   * <p>Examples:
   * <pre>{@literal
   *   int32, float64, ptr<int>
   *   func.func<(int, string) -> (bool)>, ptr<func.func<(int) -> (int)>>
   * }</pre>
   *
   * @param parameterString The comma-separated parameter string.
   * @return The list of created Type instances.
   */
  public static List<Type> fromParameterString(String parameterString) {
    List<Type> types = new java.util.ArrayList<>();
    int bracketLevel = 0;
    StringBuilder currentType = new StringBuilder();
    for (int i = 0; i < parameterString.length(); i++) {
      char c = parameterString.charAt(i);
      if (c == '<') bracketLevel++;
      if (c == '>') bracketLevel--;
      if (c == ',' && bracketLevel == 0) {
        types.add(fromParameterizedIdent(currentType.toString().trim()));
        currentType.setLength(0);
      } else {
        currentType.append(c);
      }
    }
    if (!currentType.isEmpty()) {
      types.add(fromParameterizedIdent(currentType.toString().trim()));
    }
    return types;
  }

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TypeDetails other && this.impl == other.impl;
  }

  @Override
  public int hashCode() {
    return impl.hashCode();
  }

  // =========================================================================
  // Inner: Impl
  // =========================================================================

  /**
   * Fully type-erased description of a type kind.
   * Subclasses are created per type class inside each type's {@code createImpl()} method.
   */
  public abstract static class Impl {

    protected @Nullable Type defaultInstance;
    protected @NotNull String ident;
    protected @NotNull Class<? extends Type> type;
    protected @Nullable Dialect dialect;
    protected @NotNull Constructor<? extends Type> constructor;

    public Impl(@Nullable Type defaultInstance, @NotNull String ident, @NotNull Class<? extends Type> type, @Nullable Dialect dialect) {
      this.defaultInstance = defaultInstance;
      this.ident = ident;
      this.type = type;
      this.dialect = dialect;
      // Pre-fetch the default constructor to fail fast and avoid repeated reflection lookups
      try {
        this.constructor = type.getDeclaredConstructor();
      } catch (NoSuchMethodException e) {
        throw new RuntimeException("Type class must have a default constructor: " + type.getSimpleName(), e);
      }
    }

    public @NotNull String getIdent() {
      return ident;
    }

    public @NotNull Class<? extends Type> getType() {
      return type;
    }

    public @Nullable Dialect getDialect() {
      return dialect;
    }

    /**
     * Get the parameterized ident for this type. Simple types return just the ident;
     * generic types (e.g. {@link core.ir.Type} parameterized) override this to include parameters.
     * <p>
     * Syntax:
     * <pre>
     * parameterizedType:
     *    ident
     *    | ident '&lt;' typeParameter (',' typeParameter)* '&gt;'
     *    | ident '&lt;' verbatim '&gt;'
     * </pre>
     *
     * @param type The concrete type instance to generate the ident for.
     * @return The parameterized ident string.
     */
    public @NotNull String getParameterizedIdent(@NotNull Type type) {
      return getIdent();
    }

    /**
     * Reconstruct a Type instance from its parameterized ident string.
     * Simple types return their default instance; generic types override this.
     *
     * @param parameterizedIdent The parameterized ident string.
     * @return The reconstructed Type instance.
     */
    public @NotNull Type fromParameterizedIdent(@NotNull String parameterizedIdent) {
      assert defaultInstance != null
        : "Cannot create type from parameterized ident without a default instance: " + getIdent()
        + "\nReceived parameterized ident: " + parameterizedIdent
        + "\nMake sure the type details are registered.";

      return defaultInstance;
    }

    public <T extends Type> Optional<T> fromParameterizedIdent(@NotNull String parameterizedIdent, @NotNull Class<T> clazz) {
      assert defaultInstance != null
        : "Cannot create type from parameterized ident without a default instance: " + getIdent()
        + "\nReceived parameterized ident: " + parameterizedIdent
        + "\nExpected type class: " + clazz.getSimpleName()
        + "\nMake sure the type details are registered.";

      assert clazz.isAssignableFrom(defaultInstance.getClass())
        : "Cannot create type of class " + clazz.getSimpleName()
        + " from the impl of type " + defaultInstance.getClass().getSimpleName();

      Type type = fromParameterizedIdent(parameterizedIdent);
      if (!clazz.isInstance(type))
        return Optional.empty();
      return Optional.of(clazz.cast(type));
    }

    public <T extends Type> Optional<T> createInstance(@NotNull Class<T> clazz) {
      try {
        Type type = constructor.newInstance();
        if (!clazz.isInstance(type))
          return Optional.empty();
        return Optional.of(clazz.cast(type));
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException("Failed to create instance of type: " + type.getSimpleName(), e);
      }
    }
  }

  // =========================================================================
  // Inner: UnregisteredType
  // =========================================================================

  /**
   * Placeholder used when a type ident is referenced before registration.
   */
  protected static final class UnregisteredType extends Impl {
    UnregisteredType(String ident, Class<? extends Type> clazz, Dialect dialect) {
      super(null, ident, clazz, dialect);
    }
  }
}

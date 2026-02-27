package core.ir;

import core.DGIRContext;
import core.Dialect;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Optional;

/**
 * Describes an attribute kind and exposes its metadata through a stable interface.
 *
 * <p>Two sealed implementations exist:
 *
 * <ul>
 *   <li>{@link Registered} — fully populated once a dialect's {@link core.Dialect#init()} call
 *       invokes {@link Registered#insert(Attribute)} for every contributed attribute.
 *   <li>{@link Unregistered} — a lightweight placeholder created the first time an attribute ident
 *       or class is referenced before the owning dialect has been initialised. Most accessors on
 *       this implementation throw {@link IllegalStateException}.
 * </ul>
 *
 * <p>Callers should always use the static factory methods {@link #get(String)} and
 * {@link #get(Class)} rather than constructing instances directly, so that the global
 * {@link core.DGIRContext} caches are kept consistent.
 */
public sealed interface AttributeDetails {

  // =========================================================================
  // Static Factories
  // =========================================================================

  /**
   * Look up the {@link AttributeDetails} for the given ident string. The registered registry is
   * checked first; if not found, the unregistered cache is consulted, and a new
   * {@link Unregistered} placeholder is created and cached if this is the first reference to that
   * ident.
   *
   * <p><strong>Note:</strong> this method has the side effect of populating the global
   * {@link core.DGIRContext} caches when a new placeholder is created.
   *
   * @param ident the attribute ident string (e.g. {@code "integerAttr"}).
   * @return the details instance, never {@code null}.
   */
  static @NotNull AttributeDetails get(@NotNull String ident) {
    // Try the registered registry first
    Registered registered = DGIRContext.registeredAttributesByIdent.get(ident);
    if (registered != null) {
      return registered;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
    AttributeDetails unregistered = DGIRContext.attributesByIdent.get(ident);
    if (unregistered != null) {
      return unregistered;
    }

    unregistered =
        DGIRContext.attributesByIdent.computeIfAbsent(
            ident,
            idnt -> new Unregistered(idnt, Attribute.class, DGIRContext.getReferencedDialect(idnt)));
    DGIRContext.attributes.put(Attribute.class, unregistered);
    return unregistered;
  }

  /**
   * Look up the {@link AttributeDetails} for the given attribute class. The registered registry is
   * checked first; if not found, the unregistered cache is consulted, and a new
   * {@link Unregistered} placeholder is created and cached if this is the first reference to that
   * class.
   *
   * <p><strong>Note:</strong> this method has the side effect of populating the global
   * {@link core.DGIRContext} caches when a new placeholder is created.
   *
   * @param clazz the attribute class to look up.
   * @return the details instance, never {@code null}.
   */
  static @NotNull AttributeDetails get(@NotNull Class<? extends Attribute> clazz) {
    // Try the registered registry first
    Registered registered = DGIRContext.registeredAttributes.get(clazz);
    if (registered != null) {
      return registered;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
    AttributeDetails unregistered = DGIRContext.attributes.get(clazz);
    if (unregistered != null) {
      return unregistered;
    }

    unregistered =
        DGIRContext.attributesByIdent.computeIfAbsent(
            clazz.getName(),
            idnt -> new Unregistered(clazz.getName(), Attribute.class, Optional.empty()));
    DGIRContext.attributes.put(clazz, unregistered);
    return unregistered;
  }

  /**
   * Retrieve the no-arg constructor of the given attribute class.
   *
   * @param clazz the attribute class to inspect.
   * @return the declared no-arg {@link Constructor}.
   * @throws RuntimeException if the class does not declare a no-arg constructor.
   */
  @Contract(pure = true)
  static @NotNull Constructor<? extends Attribute> getDefaultConstructor(
      @NotNull Class<? extends Attribute> clazz) {
    try {
      return clazz.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(
          "Attribute class must have a default constructor: " + clazz.getSimpleName(), e);
    }
  }

  // =========================================================================
  // Delegates
  // =========================================================================

  /**
   * The unique identifier string for this attribute kind (e.g. {@code "integerAttr"}).
   *
   * @return the ident string, never {@code null}.
   */
  @Contract(pure = true)
  @NotNull
  String ident();

  /**
   * The Java class that represents this attribute kind.
   *
   * @return the attribute class, never {@code null}.
   */
  @Contract(pure = true)
  @NotNull
  Class<? extends Attribute> type();

  /**
   * The dialect that contributes this attribute kind.
   *
   * @return the owning {@link Dialect}.
   * @throws IllegalStateException if called on an {@link Unregistered} placeholder.
   */
  @Contract(pure = true)
  @NotNull
  Dialect dialect();

  /**
   * Check whether this attribute kind matches the given class.
   *
   * @param clazz the class to check for.
   * @return {@code true} if this details instance describes {@code clazz}.
   */
  @Contract(pure = true)
  default boolean isa(@NotNull Class<? extends Attribute> clazz) {
    return clazz.equals(type());
  }

  // =========================================================================
  // Registered
  // =========================================================================

  /**
   * Fully populated description of an attribute kind. Instances are created via
   * {@link #create(Attribute)} and registered into the global {@link core.DGIRContext} caches by
   * {@link #insert(Attribute)}, which is called for every attribute contributed by a dialect during
   * {@link core.Dialect#init()}.
   */
  record Registered(
      @NotNull String ident,
      @NotNull Class<? extends Attribute> type,
      @NotNull Dialect dialect,
      @NotNull Constructor<? extends Attribute> constructor)
      implements AttributeDetails {

    /**
     * Build a {@link Registered} instance from an {@link Attribute} prototype. All fields are
     * derived by introspecting the attribute's class and the values returned by its abstract
     * methods.
     *
     * <p>The owning dialect must already be registered in {@link core.DGIRContext} before this
     * method is called, because {@link core.Dialect#getOrThrow(Class)} is used to resolve it.
     *
     * @param attr an attribute prototype; must not be {@code null}.
     * @return a fully populated {@link Registered} instance.
     * @throws RuntimeException if the attribute class is missing a no-arg constructor.
     */
    public static @NotNull Registered create(@NotNull Attribute attr) {
      final var ident = attr.getIdent();
      final var type = attr.getClass();
      final var dialect = Dialect.getOrThrow(attr.getDialect());
      final var constructor = getDefaultConstructor(type);
      return new Registered(ident, type, dialect, constructor);
    }

    // =========================================================================
    // Static Registration
    // =========================================================================

    /**
     * Register the given attribute prototype into the global {@link core.DGIRContext} caches. If
     * the attribute already carries a {@link Registered} details instance it is reused; otherwise
     * {@link #create(Attribute)} is called first.
     *
     * <p>This method populates both the unregistered caches (so look-ups that arrive before full
     * dialect initialisation still resolve) and the registered caches (used for all post-init
     * look-ups).
     *
     * @param attr the attribute prototype to register; must not be {@code null}.
     */
    public static void insert(@NotNull Attribute attr) {
      Registered details;
      if (attr.getDetails() instanceof Registered existing) {
        details = existing;
      } else {
        details = create(attr);
      }

      // Populate the unregistered caches so look-ups before registration still resolve
      DGIRContext.attributes.put(details.type(), details);
      DGIRContext.attributesByIdent.put(details.ident(), details);

      // Populate the registered caches
      DGIRContext.registeredAttributes.put(details.type(), details);
      DGIRContext.registeredAttributesByIdent.put(details.ident(), details);
    }

    // =========================================================================
    // Static Lookups
    // =========================================================================

    /**
     * Look up a {@link Registered} entry by attribute class.
     *
     * @param clazz the attribute class to look up.
     * @return the registered details, or empty if the class has not been registered yet.
     */
    @Contract(pure = true)
    public static @NotNull Optional<Registered> lookup(
        @NotNull Class<? extends Attribute> clazz) {
      return Optional.ofNullable(DGIRContext.registeredAttributes.get(clazz));
    }

    /**
     * Look up a {@link Registered} entry by attribute ident string.
     *
     * @param name the ident string (e.g. {@code "integerAttr"}) to look up.
     * @return the registered details, or empty if the ident has not been registered yet.
     */
    @Contract(pure = true)
    public static @NotNull Optional<Registered> lookup(@NotNull String name) {
      return Optional.ofNullable(DGIRContext.registeredAttributesByIdent.get(name));
    }
  }

  // =========================================================================
  // Unregistered
  // =========================================================================

  /**
   * Placeholder created the first time an attribute ident or class is referenced before its owning
   * dialect has been initialised.
   *
   * <p>The {@code type} field is always set to {@link core.ir.Attribute Attribute.class} because
   * the concrete subclass is not yet known. The {@code dialectOpt} field is populated by
   * {@link core.DGIRContext#getReferencedDialect(String)} when the ident contains a namespace
   * prefix, and left empty when the placeholder is created from a bare class name.
   *
   * <p>Only {@link #ident()}, {@link #type()}, and {@link #dialect()} (when a dialect could be
   * resolved) are usable on this placeholder; all other accessors throw
   * {@link IllegalStateException}.
   */
  record Unregistered(
      @NotNull String ident,
      @NotNull Class<? extends Attribute> type,
      @NotNull Optional<Dialect> dialectOpt)
      implements AttributeDetails {

    @Override
    public @NotNull Dialect dialect() {
      if (dialectOpt.isPresent()) return dialectOpt.get();
      throw new IllegalStateException("Cannot get dialect for unregistered attribute: " + ident);
    }
  }
}

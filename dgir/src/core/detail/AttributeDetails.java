package core.detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.DGIRContext;
import core.Dialect;
import core.ir.Attribute;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds all basic information about an attribute kind and exposes it through a stable interface.
 * The actual data lives in the inner {@link Impl}.
 */
public class AttributeDetails {

  // =========================================================================
  // Static Factories
  // =========================================================================

  @Contract(pure = true)
  public static @NotNull AttributeDetails get(@NotNull String ident) {
    return new AttributeDetails(ident);
  }

  @Contract(pure = true)
  public static @NotNull AttributeDetails get(@NotNull Class<? extends Attribute> clazz) {
    return new AttributeDetails(clazz);
  }

  // =========================================================================
  // Members
  // =========================================================================

  private final @NotNull Impl impl;

  // =========================================================================
  // Constructors
  // =========================================================================

  protected AttributeDetails(@NotNull AttributeDetails.Impl impl) {
    this.impl = impl;
  }

  /** Look up or create an {@link AttributeDetails} by ident string. */
  public AttributeDetails(@NotNull String ident) {
    // Try the registered registry first
    AttributeDetails registeredDetails = DGIRContext.registeredAttributesByIdent.get(ident);
    if (registeredDetails != null) {
      impl = registeredDetails.impl;
      return;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
    Impl unregisteredDetails = DGIRContext.attributesByIdent.get(ident);
    if (unregisteredDetails != null) {
      impl = unregisteredDetails;
      return;
    }

    unregisteredDetails =
        DGIRContext.attributesByIdent.computeIfAbsent(
            ident,
            idnt -> new UnregisteredAttributeModel(idnt, DGIRContext.getReferencedDialect(idnt)));
    DGIRContext.attributes.put(Attribute.class, unregisteredDetails);
    impl = unregisteredDetails;
  }

  /** Look up or create an {@link AttributeDetails} by attribute class. */
  public AttributeDetails(@NotNull Class<? extends Attribute> clazz) {
    // Try the registered registry first
    AttributeDetails registeredName = DGIRContext.registeredAttributes.get(clazz);
    if (registeredName != null) {
      impl = registeredName.impl;
      return;
    }

    // Fall back to the unregistered cache; create a dummy entry if absent
    Impl unregisteredName = DGIRContext.attributes.get(clazz);
    if (unregisteredName != null) {
      impl = unregisteredName;
      return;
    }

    unregisteredName =
        DGIRContext.attributesByIdent.computeIfAbsent(
            clazz.getName(), idnt -> new UnregisteredAttributeModel(clazz.getName(), null));
    DGIRContext.attributes.put(clazz, unregisteredName);
    impl = unregisteredName;
  }

  // =========================================================================
  // Delegates
  // =========================================================================

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull AttributeDetails.Impl getImpl() {
    return impl;
  }

  @Contract(pure = true)
  public @NotNull String getIdent() {
    return impl.getIdent();
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Class<? extends Attribute> getType() {
    return impl.getType();
  }

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<Dialect> getDialect() {
    return impl.getDialect();
  }

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public boolean equals(@Nullable Object obj) {
    return obj instanceof AttributeDetails other && this.impl == other.impl;
  }

  @Override
  public int hashCode() {
    return impl.hashCode();
  }

  // =========================================================================
  // Inner: Impl
  // =========================================================================

  /**
   * Fully type-erased description of an attribute kind. Subclasses are created per attribute class
   * inside each attribute's {@code createImpl()} method.
   */
  public abstract static class Impl {

    protected @NotNull String ident;
    protected @NotNull Class<? extends Attribute> type;
    protected @Nullable Dialect dialect;

    public Impl(
        @NotNull String ident,
        @NotNull Class<? extends Attribute> type,
        @Nullable Dialect dialect) {
      this.ident = ident;
      this.type = type;
      this.dialect = dialect;
    }

    @Contract(pure = true)
    public @NotNull String getIdent() {
      return ident;
    }

    @Contract(pure = true)
    public @NotNull Class<? extends Attribute> getType() {
      return type;
    }

    @Contract(pure = true)
    public @NotNull Optional<Dialect> getDialect() {
      return Optional.ofNullable(dialect);
    }
  }

  // =========================================================================
  // Inner: UnregisteredAttributeModel
  // =========================================================================

  /** Placeholder used when an attribute ident is referenced before registration. */
  protected static final class UnregisteredAttributeModel extends AttributeDetails.Impl {
    UnregisteredAttributeModel(@NotNull String ident, @Nullable Dialect dialect) {
      super(ident, Attribute.class, dialect);
    }
  }
}

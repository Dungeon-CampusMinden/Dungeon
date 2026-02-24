package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.Attribute;
import core.ir.Type;
import dialect.builtin.BuiltinDialect;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Attribute that wraps a {@link Type} instance as an IR attribute.
 *
 * <p>Ident: {@code typeAttr}. Used by operations such as {@link dialect.func.FuncOp} to embed the
 * full function type into the operation's attribute dictionary.
 */
public class TypeAttribute extends Attribute {

  // =========================================================================
  // Static Fields
  // =========================================================================

  /** Prototype instance used during dialect registration. */
  public static final @NotNull TypeAttribute INSTANCE = new TypeAttribute();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull AttributeDetails.Impl createImpl() {
    class TypeAttributeModel extends AttributeDetails.Impl {
      TypeAttributeModel() {
        super(TypeAttribute.getIdent(), TypeAttribute.class, Dialect.getOrThrow(BuiltinDialect.class));
      }
    }
    return new TypeAttributeModel();
  }

  /** Returns the ident string {@code "typeAttr"}. */
  @Contract(pure = true)
  public static @NotNull String getIdent() {
    return "typeAttr";
  }

  /** Returns the namespace of this attribute ({@code ""}, the builtin namespace). */
  @Contract(pure = true)
  public static @NotNull String getNamespace() {
    return "";
  }

  // =========================================================================
  // Members
  // =========================================================================

  /** The wrapped type, or {@code null} if unset. */
  private @Nullable Type type;

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Create a default type attribute with a {@code null} type. */
  public TypeAttribute() {}

  /**
   * Create a type attribute wrapping the given type.
   *
   * @param type the type to wrap; may be {@code null}.
   */
  public TypeAttribute(@Nullable Type type) {
    this.type = type;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the wrapped type, or {@code null} if not set.
   *
   * @return the wrapped type.
   */
  @Contract(pure = true)
  @Override
  public @Nullable Type getStorage() {
    return type;
  }

  /**
   * Returns the wrapped type as an {@link Optional}.
   *
   * @return an optional containing the wrapped type, or empty if unset.
   */
  @Contract(pure = true)
  public @NotNull Optional<Type> getType() {
    return Optional.ofNullable(type);
  }

  /**
   * Updates the wrapped type.
   *
   * @param type the new type; may be {@code null} to clear the attribute.
   */
  public void setType(@Nullable Type type) {
    this.type = type;
  }
}

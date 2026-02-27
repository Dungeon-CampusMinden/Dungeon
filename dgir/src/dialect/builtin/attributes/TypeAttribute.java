package dialect.builtin.attributes;

import core.*;
import core.ir.Attribute;
import core.ir.Type;
import dialect.builtin.BuiltinDialect;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Attribute that wraps a {@link Type} instance as an IR attribute.
 *
 * <p>Ident: {@code typeAttr}. Used by operations such as {@link dialect.func.FuncOp} to embed the
 * full function type into the operation's attribute dictionary.
 */
public class TypeAttribute extends Attribute {
  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  @Contract(pure = true)
  public @NotNull String getIdent() {
    return "typeAttr";
  }

  @Override
  @Contract(pure = true)
  public @NotNull String getNamespace() {
    return "";
  }

  @Override
  @Contract(pure = true)
  public @NotNull Class<? extends Dialect> getDialect() {
    return BuiltinDialect.class;
  }

  // =========================================================================
  // Members
  // =========================================================================

  /** The wrapped type, or {@code null} if unset. */
  private @NotNull Type type;

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Create a default type attribute with a {@code null} type. */
  public TypeAttribute() {
    type = IntegerT.INT64;
  }

  /**
   * Create a type attribute wrapping the given type.
   *
   * @param type the type to wrap; may be {@code null}.
   */
  public TypeAttribute(@NotNull Type type) {
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
  public @NotNull Type getStorage() {
    return type;
  }

  /**
   * Returns the wrapped type as an {@link Optional}.
   *
   * @return an optional containing the wrapped type, or empty if unset.
   */
  @Contract(pure = true)
  public @NotNull Type getType() {
    return type;
  }

  /**
   * Updates the wrapped type.
   *
   * @param type the new type; may be {@code null} to clear the attribute.
   */
  public void setType(@NotNull Type type) {
    this.type = type;
  }
}

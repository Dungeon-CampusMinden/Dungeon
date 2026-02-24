package dialect.builtin.attributes;

import core.*;
import core.ir.TypedAttribute;
import dialect.builtin.BuiltinDialect;
import dialect.builtin.types.StringT;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Attribute that carries a Java {@link String} value.
 *
 * <p>Ident: {@code stringAttr}. The stored value is a plain Java {@code String}.
 */
public class StringAttribute extends TypedAttribute {

  // =========================================================================
  // Static Fields
  // =========================================================================

  /** Prototype instance used during dialect registration. */
  public static final StringAttribute INSTANCE = new StringAttribute();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  @Contract(pure = true)
  public @NotNull String getIdent() {
    return "stringAttr";
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

  /** The string value stored by this attribute. */
  private String value;

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Create a default string attribute with a {@code null} value. */
  public StringAttribute() {
    super(StringT.INSTANCE);
  }

  /**
   * Create a string attribute with the given value.
   *
   * @param value the string value to store.
   */
  public StringAttribute(@NotNull String value) {
    super(StringT.INSTANCE);
    this.value = value;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @Nullable Object getStorage() {
    return value;
  }

  /**
   * Returns the string value held by this attribute.
   *
   * @return the string value.
   */
  @Contract(pure = true)
  public String getValue() {
    return value;
  }

  /**
   * Updates the string value.
   *
   * @param value the new string value.
   */
  public void setValue(String value) {
    this.value = value;
  }
}

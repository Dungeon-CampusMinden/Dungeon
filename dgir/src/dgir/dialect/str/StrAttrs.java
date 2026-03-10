package dgir.dialect.str;

import dgir.core.Dialect;
import dgir.core.ir.Type;
import dgir.core.ir.TypedAttribute;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed interface StrAttrs {
  abstract class StrAttribute extends TypedAttribute {
    protected StrAttribute(@NotNull Type type) {
      super(type);
    }

    @Override
    public @NotNull String getNamespace() {
      return "str";
    }

    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return StrDialect.class;
    }
  }

  /**
   * Attribute that carries a Java {@link String} value.
   *
   * <p>Ident: {@code stringAttr}. The stored value is a plain Java {@code String}.
   */
  final class StringAttribute extends StrAttribute implements StrAttrs {
    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    @Contract(pure = true)
    public @NotNull String getIdent() {
      return "stringAttr";
    }

    // =========================================================================
    // Members
    // =========================================================================

    /** The string value stored by this attribute. */
    private @NotNull String value;

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Create a default string attribute with a {@code null} value. */
    public StringAttribute() {
      super(StrTypes.StringT.INSTANCE);
      value = "";
    }

    /**
     * Create a string attribute with the given value.
     *
     * @param value the string value to store.
     */
    public StringAttribute(@NotNull String value) {
      super(StrTypes.StringT.INSTANCE);
      this.value = value;
    }

    // =========================================================================
    // Functions
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull Object getStorage() {
      return value;
    }

    /**
     * Returns the string value held by this attribute.
     *
     * @return the string value.
     */
    @Contract(pure = true)
    public @NotNull String getValue() {
      return value;
    }

    /**
     * Sets the string value of this attribute.
     *
     * @param value the new string value.
     */
    public void setValue(@NotNull String value) {
      this.value = value;
    }
  }
}

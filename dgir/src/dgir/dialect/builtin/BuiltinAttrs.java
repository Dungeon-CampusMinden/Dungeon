package dgir.dialect.builtin;

import static dgir.dialect.builtin.BuiltinTypes.*;
import static dgir.dialect.func.FuncOps.CallOp;
import static dgir.dialect.func.FuncOps.FuncOp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dgir.core.Dialect;
import dgir.core.ir.Attribute;
import dgir.core.ir.Type;
import dgir.core.ir.TypedAttribute;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public sealed interface BuiltinAttrs {
  abstract class BuiltinBaseAttr extends Attribute {
    @Override
    public @NotNull String getNamespace() {
      return "";
    }

    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return BuiltinDialect.class;
    }
  }

  abstract class BuiltinBaseTypedAttr extends TypedAttribute {
    /**
     * Create a typed attribute associated with the given type.
     *
     * @param type the type that governs validation of the stored value.
     */
    protected BuiltinBaseTypedAttr(@NotNull Type type) {
      super(type);
    }

    @Override
    public @NotNull String getNamespace() {
      return "";
    }

    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return BuiltinDialect.class;
    }
  }

  /**
   * Attribute that carries an integer value together with its {@link IntegerT} type.
   *
   * <p>Ident: {@code integerAttr}. The stored value is always the narrowest Java numeric type that
   * matches the integer width — e.g. {@link Integer} for {@link IntegerT#INT32}.
   */
  final class IntegerAttribute extends BuiltinBaseTypedAttr implements BuiltinAttrs {
    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    @Contract(pure = true)
    public @NotNull String getIdent() {
      return "integerAttr";
    }

    // =========================================================================
    // Members
    // =========================================================================

    /** The integer value stored by this attribute. */
    private @NotNull Number value;

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Create a default integer attribute (value {@code null}, type {@link IntegerT#INT64}). */
    public IntegerAttribute() {
      super(IntegerT.INT64);
      value = 0L;
    }

    /**
     * Create an integer attribute with the given value and the default type {@link IntegerT#INT64}.
     *
     * @param value the integer value.
     */
    public IntegerAttribute(@NotNull Number value) {
      super(IntegerT.INT64);
      this.value = ((IntegerT) getType()).convertToValidNumber(value);
    }

    /**
     * Create an integer attribute with an explicit value and type (used during JSON
     * deserialization).
     *
     * @param value the integer value; will be converted to the correct Java type via {@link
     *     IntegerT#convertToValidNumber(Number)}.
     * @param type the integer type that determines the bit-width.
     */
    @JsonCreator
    public IntegerAttribute(
        @JsonProperty("value") Number value, @JsonProperty("type") IntegerT type) {
      super(type);
      this.value = ((IntegerT) getType()).convertToValidNumber(value);
    }

    // =========================================================================
    // Functions
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull Number getStorage() {
      return getValue();
    }

    /**
     * Returns the integer value held by this attribute.
     *
     * @return the numeric value.
     */
    @Contract(pure = true)
    public @NotNull Number getValue() {
      return value;
    }

    /**
     * Sets the integer value of this attribute. The provided value will be converted to the correct
     * Java type based on the attribute's {@link IntegerT} type.
     *
     * @param value the new integer value.
     */
    public void setValue(@NotNull Number value) {
      this.value = ((IntegerT) getType()).convertToValidNumber(value);
    }
  }

  final class FloatAttribute extends BuiltinBaseTypedAttr implements BuiltinAttrs {
    @Override
    @Contract(pure = true)
    public @NotNull String getIdent() {
      return "floatAttr";
    }

    private @NotNull Number value;

    public FloatAttribute() {
      super(FloatT.FLOAT64);
      value = 0.0;
    }

    public FloatAttribute(@NotNull Number value) {
      super(FloatT.FLOAT64);
      this.value = ((FloatT) getType()).convertToValidNumber(value);
    }

    @JsonCreator
    public FloatAttribute(
        @JsonProperty("value") @NotNull Number value, @JsonProperty("type") @NotNull FloatT type) {
      super(type);
      this.value = ((FloatT) getType()).convertToValidNumber(value);
    }

    @Contract(pure = true)
    @Override
    public @NotNull Number getStorage() {
      return value;
    }

    @Contract(pure = true)
    public @NotNull Number getValue() {
      return value;
    }

    public void setValue(@NotNull Number value) {
      this.value = ((FloatT) getType()).convertToValidNumber(value);
    }
  }

  /**
   * Attribute that carries a Java {@link String} value.
   *
   * <p>Ident: {@code stringAttr}. The stored value is a plain Java {@code String}.
   */
  final class StringAttribute extends BuiltinBaseTypedAttr implements BuiltinAttrs {
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
      super(StringT.INSTANCE);
      value = "";
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

  /**
   * Attribute that holds a reference to a symbol by its string name.
   *
   * <p>Ident: {@code symbolRefAttr}. Used by operations such as {@link CallOp} to record the name
   * of a callee function without hard-linking the IR nodes together.
   */
  final class SymbolRefAttribute extends BuiltinBaseAttr implements BuiltinAttrs {
    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    @Contract(pure = true)
    public @NotNull String getIdent() {
      return "symbolRefAttr";
    }

    // =========================================================================
    // Members
    // =========================================================================

    /** The referenced symbol name. */
    private @NotNull String value;

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Create a default symbol reference attribute with a {@code null} name. */
    public SymbolRefAttribute() {
      value = "";
    }

    /**
     * Create a symbol reference attribute pointing to the given name.
     *
     * @param value the symbol name to reference.
     */
    public SymbolRefAttribute(@NotNull String value) {
      this.value = value;
    }

    // =========================================================================
    // Functions
    // =========================================================================

    /**
     * Returns the referenced symbol name.
     *
     * @return the symbol name, or {@code null} if not set.
     */
    @Contract(pure = true)
    @Override
    public @NotNull String getStorage() {
      return value;
    }

    /**
     * Returns the referenced symbol name.
     *
     * @return the symbol name.
     */
    @Contract(pure = true)
    public @NotNull String getValue() {
      return value;
    }

    public void setValue(@NotNull String value) {
      this.value = value;
    }
  }

  /**
   * Attribute that wraps a {@link Type} instance as an IR attribute.
   *
   * <p>Ident: {@code typeAttr}. Used by operations such as {@link FuncOp} to embed the full
   * function type into the operation's attribute dictionary.
   */
  final class TypeAttribute extends BuiltinBaseAttr implements BuiltinAttrs {
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

    /** Sets the wrapped type. */
    public void setType(@NotNull Type type) {
      this.type = type;
    }
  }
}

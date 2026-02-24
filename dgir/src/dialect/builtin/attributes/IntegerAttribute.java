package dialect.builtin.attributes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.*;
import core.ir.TypedAttribute;
import dialect.builtin.BuiltinDialect;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Attribute that carries an integer value together with its {@link IntegerT} type.
 *
 * <p>Ident: {@code integerAttr}. The stored value is always the narrowest Java numeric type that
 * matches the integer width — e.g. {@link Integer} for {@link IntegerT#INT32}.
 */
public class IntegerAttribute extends TypedAttribute {

  // =========================================================================
  // Static Fields
  // =========================================================================

  /** Prototype instance used during dialect registration. */
  public static final IntegerAttribute INSTANCE = new IntegerAttribute();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  @Contract(pure = true)
  public @NotNull String getIdent() {
    return "integerAttr";
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

  /** The integer value stored by this attribute. */
  private Number value;

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Create a default integer attribute (value {@code null}, type {@link IntegerT#INT64}). */
  public IntegerAttribute() {
    super(IntegerT.INT64);
  }

  /**
   * Create an integer attribute with the given value and the default type {@link IntegerT#INT64}.
   *
   * @param value the integer value.
   */
  public IntegerAttribute(@NotNull Number value) {
    super(IntegerT.INT64);
    this.value = value;
  }

  /**
   * Create an integer attribute with an explicit value and type (used during JSON deserialization).
   *
   * @param value the integer value; will be converted to the correct Java type via
   *              {@link IntegerT#convertToValidNumber(Number)}.
   * @param type  the integer type that determines the bit-width.
   */
  @JsonCreator
  public IntegerAttribute(
      @JsonProperty("value") Number value, @JsonProperty("type") IntegerT type) {
    super(type);
    this.value = type.convertToValidNumber(value);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @Nullable Object getStorage() {
    return getValue();
  }

  /**
   * Returns the integer value held by this attribute.
   *
   * @return the numeric value.
   */
  @Contract(pure = true)
  public Number getValue() {
    return value;
  }

  /**
   * Updates the integer value, validating it against the declared type.
   *
   * @param value the new value; must be valid for the attribute's {@link IntegerT}.
   */
  public void setValue(@NotNull Number value) {
    assert getType().validate(value) : "Value " + value + " is not valid for type " + getType();
    this.value = value;
  }
}

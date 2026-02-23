package dialect.builtin.attributes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.*;
import core.detail.AttributeDetails;
import core.ir.TypedAttribute;
import dialect.builtin.BuiltinDialect;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntegerAttribute extends TypedAttribute {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final IntegerAttribute INSTANCE = new IntegerAttribute();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public AttributeDetails.@NotNull Impl createImpl() {
    class IntegerAttributeModel extends AttributeDetails.Impl {
      IntegerAttributeModel() {
        super(
            IntegerAttribute.getIdent(), IntegerAttribute.class, Dialect.getOrThrow(BuiltinDialect.class));
      }
    }
    return new IntegerAttributeModel();
  }

  public static String getIdent() {
    return "integerAttr";
  }

  public static String getNamespace() {
    return "";
  }

  // =========================================================================
  // Members
  // =========================================================================

  private Number value;

  // =========================================================================
  // Constructors
  // =========================================================================

  public IntegerAttribute() {
    super(IntegerT.INT64);
  }

  public IntegerAttribute(Number value) {
    super(IntegerT.INT64);
    this.value = value;
  }

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

  @Contract(pure = true)
  public Number getValue() {
    return value;
  }

  public void setValue(Number value) {
    assert getType().validate(value) : "Value " + value + " is not valid for type " + getType();
    this.value = value;
  }
}

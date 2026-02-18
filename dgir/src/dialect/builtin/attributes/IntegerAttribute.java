package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.Attribute;
import core.ir.TypedAttribute;
import dialect.builtin.Builtin;
import dialect.builtin.types.IntegerT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IntegerAttribute extends TypedAttribute {
  public static final IntegerAttribute INSTANCE = new IntegerAttribute();

  // --------------------- Type Info ------------------------------
  @Override
  public AttributeDetails.Impl createImpl() {
    class IntegerAttributeModel extends AttributeDetails.Impl {
      IntegerAttributeModel() {
        super(IntegerAttribute.getIdent(), IntegerAttribute.class, Dialect.get(Builtin.class));
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
  // -----------------------------------------------------------------

  private Number value;

  public IntegerAttribute() {
    super(IntegerT.INT64);
  }

  public IntegerAttribute(Number value) {
    super(IntegerT.INT64);
    this.value = value;
  }

  @JsonCreator
  public IntegerAttribute(@JsonProperty("value") Number value, @JsonProperty("type") IntegerT type) {
    super(type);
    this.value = type.convertToValidNumber(value);
  }


  @Override
  public Number getStorage() {
    return getValue();
  }

  public Number getValue() {
    return value;
  }

  public void setValue(Number value) {
    getType().validate(value);
    this.value = value;
  }
}

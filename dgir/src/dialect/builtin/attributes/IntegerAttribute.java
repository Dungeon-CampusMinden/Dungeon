package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.Attribute;
import core.ir.ITypedAttribute;
import dialect.builtin.Builtin;
import dialect.builtin.types.IntegerT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IntegerAttribute extends Attribute implements ITypedAttribute {
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

  private IntegerT type;
  private Number value;

  public IntegerAttribute() {
  }

  public IntegerAttribute(Number value) {
    this.value = value;
    this.type = IntegerT.INT64;
  }

  @JsonCreator
  public IntegerAttribute(@JsonProperty("value") Number value, @JsonProperty("type") IntegerT type) {
    this.value = type.convertToValidNumber(value);
    this.type = type;
  }


  @Override
  public Number getStorage() {
    return getValue();
  }

  @Override
  public IntegerT getType() {
    return type;
  }

  public Number getValue() {
    return value;
  }

  public void setValue(Number value) {
    getType().validate(value);
    this.value = value;
  }
}

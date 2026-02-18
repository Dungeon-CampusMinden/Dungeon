package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.Attribute;
import core.ir.TypedAttribute;
import dialect.builtin.Builtin;
import dialect.builtin.types.StringT;

public class StringAttribute extends TypedAttribute {
  public static final StringAttribute INSTANCE = new StringAttribute();
  private String value;

  @Override
  public AttributeDetails.Impl createImpl() {
    class StringAttributeModel extends AttributeDetails.Impl {
      StringAttributeModel() {
        super(StringAttribute.getIdent(), StringAttribute.class, Dialect.get(Builtin.class));
      }
    }
    return new StringAttributeModel();
  }

  public StringAttribute() {
    super(StringT.INSTANCE);
  }

  public StringAttribute(String value) {
    super(StringT.INSTANCE);
    this.value = value;
  }

  @Override
  public String getStorage() {
    return value;
  }

  public static String getIdent() {
    return "stringAttr";
  }

  public static String getNamespace() {
    return "";
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

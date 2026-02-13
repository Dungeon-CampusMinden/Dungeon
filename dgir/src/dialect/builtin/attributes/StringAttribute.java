package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.Attribute;
import core.ir.ITypedAttribute;
import dialect.builtin.Builtin;
import dialect.builtin.types.StringT;

public class StringAttribute extends Attribute implements ITypedAttribute {
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
  }

  @Override
  public String getStorage() {
    return value;
  }

  public StringAttribute(String value) {
    this.value = value;
  }

  public static String getIdent() {
    return "stringAttr";
  }

  public static String getNamespace() {
    return "";
  }

  @Override
  public StringT getType() {
    return StringT.INSTANCE;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

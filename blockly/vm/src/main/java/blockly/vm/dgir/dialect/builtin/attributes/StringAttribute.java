package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.types.StringT;

public class StringAttribute extends Attribute {
  public static final StringAttribute INSTANCE = new StringAttribute();
  private String value;

  @Override
  public AttributeDetails.Impl createImpl() {
    class StringAttributeModel extends AttributeDetails.Impl {
      public StringAttributeModel(String name, Class<? extends Attribute> type) {
        super(name, type, null);
      }
    }
    return new StringAttributeModel(getIdent(), getClass());
  }

  public StringAttribute() {
    super(RegisteredAttributeDetails.lookup(StringT.getIdent()).orElse(null));
  }

  public StringAttribute(String value) {
    super(RegisteredAttributeDetails.lookup(getIdent()).orElseThrow());
    setValue(value);
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public Type getType() {
    return StringT.INSTANCE;
  }

  public static String getIdent() {
    return "stringAttr";
  }

  public static String getNamespace() {
    return "";
  }
}

package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.Attribute;
import blockly.vm.dgir.core.AttributeName;

public class StringAttribute extends Attribute {
  public static final StringAttribute INSTANCE = new StringAttribute();
  private String value;

  @Override
  public AttributeName.Impl createImpl() {
    class StringAttributeModel extends AttributeName.Impl {
      public StringAttributeModel(String name, Class<? extends Attribute> type) {
        super(name, type, null);
      }
    }
    return new StringAttributeModel(getIdent(), getClass());
  }

  public StringAttribute() {
  }

  public StringAttribute(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public static String getIdent() {
    return "stringAttr";
  }

  public static String getNamespace() {
    return "";
  }
}

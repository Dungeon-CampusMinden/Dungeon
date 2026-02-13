package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.Attribute;
import dialect.builtin.Builtin;

public class SymbolRefAttribute extends Attribute {
  public static final SymbolRefAttribute INSTANCE = new SymbolRefAttribute();
  private String value;

  public SymbolRefAttribute() {
  }

  @Override
  public String getStorage() {
    return value;
  }

  public SymbolRefAttribute(String value) {
    this.value = value;
  }

  @Override
  public AttributeDetails.Impl createImpl() {
    class SymbolRefAttributeModel extends AttributeDetails.Impl {
      SymbolRefAttributeModel() {
        super(SymbolRefAttribute.getIdent(), SymbolRefAttribute.class, Dialect.get(Builtin.class));
      }
    }
    return new SymbolRefAttributeModel();
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public static String getIdent() {
    return "symbolRefAttr";
  }

  public static String getNamespace() {
    return "";
  }
}

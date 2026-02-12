package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.detail.AttributeDetails;
import blockly.vm.dgir.core.ir.Attribute;
import blockly.vm.dgir.core.ir.Type;
import blockly.vm.dgir.dialect.builtin.Builtin;

public class TypeAttribute extends Attribute {
  public static final TypeAttribute INSTANCE = new TypeAttribute();
  private Type type;

  public TypeAttribute() {
  }

  @Override
  public Type getStorage() {
    return type;
  }

  public TypeAttribute(Type type) {
    this.type = type;
  }

  @Override
  public AttributeDetails.Impl createImpl() {
    class TypeAttributeModel extends AttributeDetails.Impl {
      TypeAttributeModel() {
        super(TypeAttribute.getIdent(), TypeAttribute.class, Dialect.get(Builtin.class));
      }
    }
    return new TypeAttributeModel();
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public static String getIdent() {
    return "typeAttr";
  }

  public static String getNamespace() {
    return "";
  }
}

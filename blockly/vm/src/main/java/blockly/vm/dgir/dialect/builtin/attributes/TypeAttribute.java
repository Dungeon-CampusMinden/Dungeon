package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.Builtin;

public class TypeAttribute extends Attribute {
  public static final TypeAttribute INSTANCE = new TypeAttribute();
  private Type type;

  public TypeAttribute() {
  }

  public TypeAttribute(Type type) {
    super(RegisteredAttributeName.lookup(getIdent()).orElseThrow());
    setType(type);
  }

  @Override
  public AttributeName.Impl createImpl() {
    class TypeAttributeModel extends AttributeName.Impl {
      public TypeAttributeModel(String name, Class<? extends Attribute> type, Dialect dialect) {
        super(name, type, dialect);
      }
    }
    return new TypeAttributeModel(getIdent(), getClass(), Dialect.get(Builtin.class));
  }

  @Override
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

package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.builtin.types.StringT;

public class TypeAttribute extends Attribute {
  public static final TypeAttribute INSTANCE = new TypeAttribute();
  private Type type;

  public TypeAttribute() {
    super(RegisteredAttributeDetails.lookup(StringT.getIdent()).orElse(null));
  }

  @Override
  public Object getStorage() {
    return null;
  }

  @Override
  public void setStorage(Object storage) {
    getType().validate(storage);
    setType((Type) storage);
  }

  public TypeAttribute(Type type) {
    super(RegisteredAttributeDetails.lookup(getIdent()).orElseThrow());
    setType(type);
  }

  @Override
  public AttributeDetails.Impl createImpl() {
    class TypeAttributeModel extends AttributeDetails.Impl {
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

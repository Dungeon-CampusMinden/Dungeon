package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;
import blockly.vm.dgir.dialect.builtin.types.StringT;

public class IntegerAttribute extends Attribute {
  public static final IntegerAttribute INSTANCE = new IntegerAttribute();
  private long value;

  @Override
  public AttributeDetails.Impl createImpl() {
    class IntegerAttributeModel extends AttributeDetails.Impl {
      IntegerAttributeModel(String name, Class<? extends Attribute> type) {
        super(name, type, null);
      }
    }
    return new IntegerAttributeModel(getIdent(), getClass());
  }

  public IntegerAttribute(){
  }

  @Override
  public Object getStorage() {
    return value;
  }

  @Override
  public void setStorage(Object storage) {
    getType().validate(storage);
    setValue((long) storage);
  }

  public IntegerAttribute(long value) {
    setValue(value);
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public static String getIdent() {
    return "integerAttr";
  }

  public static String getNamespace() {
    return "";
  }

  @Override
  public Type getType() {
    return IntegerT.INT64;
  }
}

package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.Attribute;
import blockly.vm.dgir.core.AttributeName;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IntegerAttribute extends Attribute {
  public static final IntegerAttribute INSTANCE = new IntegerAttribute();
  private long value;

  @Override
  public AttributeName.Impl createImpl() {
    class IntegerAttributeModel extends AttributeName.Impl {
      public IntegerAttributeModel(String name, Class<? extends Attribute> type) {
        super(name, type, null);
      }
    }
    return new IntegerAttributeModel(getIdent(), getClass());
  }

  public IntegerAttribute(){
  }

  public IntegerAttribute(long value) {
    this.value = value;
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
}

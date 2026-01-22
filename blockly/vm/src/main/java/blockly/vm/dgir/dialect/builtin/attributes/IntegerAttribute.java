package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.Attribute;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IntegerAttribute extends Attribute {
  public static final IntegerAttribute INSTANCE = new IntegerAttribute();
  private long value;

  public IntegerAttribute() {
    super(IntegerT.INT32);
  }

  public IntegerAttribute(long value, IntegerT type) {
    super(type);
    this.value = value;
  }

  @JsonCreator
  public IntegerAttribute(@JsonProperty("value") long value) {
    this.value = value;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  @Override
  public String getIdent() {
    return "IntegerAttr";
  }

  @Override
  public String getNamespace() {
    return "";
  }
}

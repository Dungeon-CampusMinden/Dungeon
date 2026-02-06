package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class IntegerAttribute extends Attribute implements ITypedAttribute {
  public static final IntegerAttribute INSTANCE = new IntegerAttribute();

  // --------------------- Type Info ------------------------------
  @Override
  public AttributeDetails.Impl createImpl() {
    class IntegerAttributeModel extends AttributeDetails.Impl {
      IntegerAttributeModel() {
        super(IntegerAttribute.getIdent(), IntegerAttribute.class, Dialect.get(Builtin.class));
      }
    }
    return new IntegerAttributeModel();
  }

  public static String getIdent() {
    return "integerAttr";
  }

  public static String getNamespace() {
    return "";
  }
  // -----------------------------------------------------------------

  private IntegerT type;
  private long value;

  public IntegerAttribute() {
  }

  public IntegerAttribute(long value) {
    this.value = value;
    this.type = IntegerT.INT64;
  }

  @JsonCreator
  public IntegerAttribute(@JsonProperty("value") long value, @JsonProperty("type") IntegerT type) {
    this.value = value;
    this.type = type;
  }


  @Override
  public Long getStorage() {
    return getValue();
  }

  @Override
  public IntegerT getType() {
    return type;
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }
}

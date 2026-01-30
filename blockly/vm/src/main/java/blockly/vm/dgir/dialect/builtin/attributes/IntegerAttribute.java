package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;

import java.util.Optional;

public class IntegerAttribute extends Attribute implements ITypedAttribute {
  public static final IntegerAttribute INSTANCE = new IntegerAttribute();
  private IntegerT type;
  private long value;

  @Override
  public AttributeDetails.Impl createImpl() {
    class IntegerAttributeModel extends AttributeDetails.Impl {
      IntegerAttributeModel() {
        super(IntegerAttribute.getIdent(), IntegerAttribute.class, Dialect.get(Builtin.class));
      }
    }
    return new IntegerAttributeModel();
  }

  public IntegerAttribute() {
  }

  @Override
  public Long getStorage() {
    return getValue();
  }

  public IntegerAttribute(long value, IntegerT type) {
    this.value = value;
    this.type = type;
  }

  public static String getIdent() {
    return "integerAttr";
  }

  public static String getNamespace() {
    return "";
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

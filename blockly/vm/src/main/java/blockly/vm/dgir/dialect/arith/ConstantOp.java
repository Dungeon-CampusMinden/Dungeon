package blockly.vm.dgir.dialect.arith;

import blockly.vm.dgir.core.*;

import java.util.List;

public class ConstantOp extends Op {
  private Attribute value;

  @Override
  public OperationDetails.Impl createDetails() {
    class ConstantOpModel extends OperationDetails.Impl {
      public ConstantOpModel() {
        super(ConstantOp.getIdent(), ConstantOp.class, Dialect.get(Arith.class), List.of("value"));
      }

      @Override
      public boolean verify(Operation operation) {
        return false;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
      }
    }
    return new ConstantOpModel();
  }

  public ConstantOp() {
  }

  public ConstantOp(Attribute value) {
    super(Operation.Create(getIdent(), null, new OperationResult(value.getType()), null));
    setValueAttribute(value);
  }

  public Attribute getValueAttribute() {
    if (value == null) {
      value = getOperation().getAttributes().get("value").getAttribute();
    }
    return value;
  }

  public void setValueAttribute(Attribute attribute) {
    getOperation().getAttributes().get("value").setAttribute(attribute);
    this.value = attribute;
  }

  public static String getIdent() {
    return "artih.constant";
  }

  public static String getNamespace() {
    return "arith";
  }
}

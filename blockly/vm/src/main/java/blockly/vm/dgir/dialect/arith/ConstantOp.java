package blockly.vm.dgir.dialect.arith;

import blockly.vm.dgir.core.*;

import java.util.List;

public class ConstantOp extends Op {
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
    setOperation(Operation.Create(getIdent(), null, new OperationResult(value.getType()), null));
    setValueAttribute(value);
  }

  public Attribute getValueAttribute() {
    return getAttributes().get("value").getAttribute();
  }

  public void setValueAttribute(Attribute attribute) {
    assert attribute != null : "Attribute cannot be null.";
    getOperation().getAttributes().get("value").setAttribute(attribute);
    if (getOperation().getOutput() != null)
    {
      getOperation().getOutput().setType(attribute.getType());
    }
  }

  public static String getIdent() {
    return "artih.constant";
  }

  public static String getNamespace() {
    return "arith";
  }
}

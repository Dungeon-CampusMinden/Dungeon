package blockly.vm.dgir.dialect.arith;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.detail.OperationDetails;
import blockly.vm.dgir.core.ir.*;
import blockly.vm.dgir.core.traits.ISingleOperand;

import java.util.List;

public class ConstantOp extends Op implements ISingleOperand {
  @Override
  public OperationDetails.Impl createDetails() {
    class ConstantOpModel extends OperationDetails.Impl {
      ConstantOpModel() {
        super(ConstantOp.getIdent(), ConstantOp.class, Dialect.get(Arith.class), List.of("value"));
      }

      @Override
      public boolean verify(Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
      }
    }
    return new ConstantOpModel();
  }

  public ConstantOp() {
  }

  public ConstantOp(Operation operation) {
    super(operation);
  }

  public ConstantOp(ITypedAttribute value) {
    setOperation(Operation.Create(getIdent(), null, null, value.getType()));
    getAttributes().get("value").setAttribute((Attribute) value);
  }

  public ConstantOp(String value) {
    this(new blockly.vm.dgir.dialect.builtin.attributes.StringAttribute(value));
  }

  public ConstantOp(int value) {
    this(new blockly.vm.dgir.dialect.builtin.attributes.IntegerAttribute(value, blockly.vm.dgir.dialect.builtin.types.IntegerT.INT32));
  }

  public ITypedAttribute getValueAttribute() {
    return (ITypedAttribute) getAttributes().get("value").getAttribute();
  }

  public static String getIdent() {
    return "artih.constant";
  }

  public static String getNamespace() {
    return "arith";
  }
}

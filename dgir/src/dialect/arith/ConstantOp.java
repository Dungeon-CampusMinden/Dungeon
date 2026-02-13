package dialect.arith;

import core.*;
import core.detail.OperationDetails;
import core.ir.*;
import core.traits.ISingleOperand;
import dialect.builtin.attributes.IntegerAttribute;
import dialect.builtin.attributes.StringAttribute;
import dialect.builtin.types.IntegerT;

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
    super(true, Operation.Create(getIdent(), null, null, value.getType()));
    getAttributes().get("value").setAttribute((Attribute) value);
  }

  public ConstantOp(String value) {
    this(new StringAttribute(value));
  }

  public ConstantOp(int value) {
    this(new IntegerAttribute(value, IntegerT.INT32));
  }

  public ConstantOp(boolean value) {
    this(new IntegerAttribute(value ? 1 : 0, IntegerT.BOOL));
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

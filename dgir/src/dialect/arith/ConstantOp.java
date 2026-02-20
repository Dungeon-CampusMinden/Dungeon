package dialect.arith;

import core.*;
import core.detail.OperationDetails;
import core.ir.*;
import core.traits.ISingleOperand;
import dialect.builtin.attributes.IntegerAttribute;
import dialect.builtin.attributes.StringAttribute;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConstantOp extends Op implements ISingleOperand {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.@NotNull Impl createDetails() {
    class ConstantOpModel extends OperationDetails.Impl {
      ConstantOpModel() {
        super(ConstantOp.getIdent(), ConstantOp.class, Dialect.getOrThrow(Arith.class), List.of("value"));
      }

      @Override
      public boolean verify(@NotNull Operation operation) {
        return true;
      }

      @Override
      public void populateDefaultAttrs(@NotNull List<NamedAttribute> attributes) {
      }
    }
    return new ConstantOpModel();
  }

  public static String getIdent() {
    return "artih.constant";
  }

  public static String getNamespace() {
    return "arith";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public ConstantOp() {
  }

  public ConstantOp(Operation operation) {
    super(operation);
  }

  public ConstantOp(TypedAttribute value) {
    super(true, Operation.Create(getIdent(), null, null, value.getType()));
    getAttributes().get("value").setAttribute(value);
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

  // =========================================================================
  // Functions
  // =========================================================================

  public TypedAttribute getValueAttribute() {
    return getAttribute(TypedAttribute.class, "value").orElseThrow(() -> new AssertionError("No value attribute found."));
  }

  public Type getValueType() {
    return getValueAttribute().getType();
  }

  public Object getValueStorage() {
    return getValueAttribute().getStorage();
  }

  public Value getValue() {
    return getOutputValue().orElseThrow(() -> new AssertionError("No output value found."));
  }
}

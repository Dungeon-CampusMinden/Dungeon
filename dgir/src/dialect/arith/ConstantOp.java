package dialect.arith;

import core.*;
import core.ir.*;
import core.traits.ISingleOperand;
import dialect.builtin.attributes.IntegerAttribute;
import dialect.builtin.attributes.StringAttribute;
import dialect.builtin.types.IntegerT;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public final class ConstantOp extends ArithOp implements Arith, ISingleOperand {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "arith.constant";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  @Override
  public @NotNull java.util.List<NamedAttribute> getDefaultAttributes() {
    return List.of(new NamedAttribute("value", null));
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  private ConstantOp() {}

  public ConstantOp(Operation operation) {
    super(operation);
  }

  public ConstantOp(TypedAttribute value) {
    setOperation(true, Operation.Create(this, null, null, value.getType()));
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
    return getAttribute(TypedAttribute.class, "value")
        .orElseThrow(() -> new AssertionError("No value attribute found."));
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

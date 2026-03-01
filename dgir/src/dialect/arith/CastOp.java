package dialect.arith;

import core.debug.Location;
import core.ir.NamedAttribute;
import core.ir.Operation;
import core.ir.Type;
import core.ir.Value;
import core.traits.IHasResult;
import core.traits.ISingleOperand;
import dialect.builtin.attributes.TypeAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Function;

/**
 * Casts a numeric operand to a target numeric type.
 */
public final class CastOp extends ArithOp implements Arith, ISingleOperand, IHasResult {

  @Override
  public @NotNull String getIdent() {
    return "arith.cast";
  }

  @Override
  public @NotNull @Unmodifiable List<NamedAttribute> getDefaultAttributes() {
    return List.of(new NamedAttribute("to", new TypeAttribute()));
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return operation -> {
      var castOp = operation.as(CastOp.class).orElseThrow();
      if (!BinaryNumericOp.isNumeric(castOp.getOperandType())) {
        castOp.emitError("Cast operand must be numeric");
        return false;
      }
      Type targetType = castOp.getTargetType();
      if (!BinaryNumericOp.isNumeric(targetType)) {
        castOp.emitError("Cast target type must be numeric");
        return false;
      }
      if (!castOp.getResultType().equals(targetType)) {
        castOp.emitError("Cast result type must match the target type");
        return false;
      }
      return true;
    };
  }

  private CastOp() {}

  /**
   * Create a cast op.
   *
   * @param loc the source location of this operation.
   * @param value the value to cast.
   * @param targetType the target numeric type.
   */
  public CastOp(@NotNull Location loc, @NotNull Value value, @NotNull Type targetType) {
    setOperation(Operation.Create(loc, this, List.of(value), null, targetType));
    setAttribute("to", new TypeAttribute(targetType));
  }

  public @NotNull Type getTargetType() {
    return getAttribute("to", TypeAttribute.class)
        .map(TypeAttribute::getType)
        .orElseThrow(() -> new AssertionError("No target type attribute found."));
  }
}

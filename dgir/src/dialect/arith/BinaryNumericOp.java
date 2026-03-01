package dialect.arith;

import core.ir.Operation;
import core.ir.Type;
import core.traits.IBinaryOperands;
import dialect.builtin.types.FloatT;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/** Base class for binary numeric operations in the {@code arith} dialect. */
public abstract class BinaryNumericOp extends ArithOp implements IBinaryOperands {

  /** Default constructor used during dialect registration. */
  BinaryNumericOp() {
    super();
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return BinaryNumericOp::verifyBinaryNumericOperands;
  }

  protected static boolean verifyBinaryNumericOperands(@NotNull Operation operation) {
    IBinaryOperands binaryOperands =
        operation
            .asTrait(IBinaryOperands.class)
            .orElseThrow(() -> new AssertionError("Operation does not implement IBinaryOperands: " + operation));
    Type lhsType = binaryOperands.getLhs().getType();
    Type rhsType = binaryOperands.getRhs().getType();
    if (!isNumeric(lhsType) || !isNumeric(rhsType)) {
      operation.emitError("Operands must be numeric");
      return false;
    }
    return true;
  }

  protected static boolean isNumeric(@NotNull Type type) {
    return type instanceof IntegerT || type instanceof FloatT;
  }

  protected static @NotNull Type getDominantType(@NotNull Type lhsType, @NotNull Type rhsType) {
    if (!isNumeric(lhsType) || !isNumeric(rhsType)) {
      throw new IllegalArgumentException("Dominant type requires numeric operands");
    }

    if (lhsType instanceof FloatT || rhsType instanceof FloatT) {
      int lhsFloatWidth = lhsType instanceof FloatT floatT ? floatT.getWidth() : 0;
      int rhsFloatWidth = rhsType instanceof FloatT floatT ? floatT.getWidth() : 0;
      int lhsIntWidth = lhsType instanceof IntegerT intT ? intT.getWidth() : 0;
      int rhsIntWidth = rhsType instanceof IntegerT intT ? intT.getWidth() : 0;
      int desiredWidth =
          Math.max(Math.max(lhsFloatWidth, rhsFloatWidth), Math.max(lhsIntWidth, rhsIntWidth));
      return desiredWidth > 32 ? FloatT.FLOAT64 : FloatT.FLOAT32;
    }

    int lhsWidth = ((IntegerT) lhsType).getWidth();
    int rhsWidth = ((IntegerT) rhsType).getWidth();
    return integerTypeByWidth(Math.max(lhsWidth, rhsWidth));
  }

  protected static @NotNull IntegerT integerTypeByWidth(int width) {
    return switch (width) {
      case 1 -> IntegerT.INT1;
      case 8 -> IntegerT.INT8;
      case 16 -> IntegerT.INT16;
      case 32 -> IntegerT.INT32;
      case 64 -> IntegerT.INT64;
      default -> new IntegerT(width);
    };
  }
}

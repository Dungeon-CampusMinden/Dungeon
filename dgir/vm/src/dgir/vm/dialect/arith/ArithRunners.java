package dgir.vm.dialect.arith;

import dgir.core.ir.Operation;
import dgir.core.ir.Type;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dgir.dialect.arith.ArithAttrs;
import dgir.dialect.arith.ArithOps;
import dgir.dialect.builtin.BuiltinTypes;
import org.jetbrains.annotations.NotNull;

public sealed interface ArithRunners {
  /**
   * Executes {@code arith.bin} operations.
   *
   * <p>The runtime dispatch is mode-first (ADD/SUB/..., LT/LE/..., DIVUI/...) and then type-aware,
   * so signed, unsigned, and floating-point semantics are applied explicitly.
   */
  final class BinaryRunner extends OpRunner implements ArithRunners {
    public BinaryRunner() {
      super(ArithOps.BinaryOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      ArithOps.BinaryOp binOp = op.as(ArithOps.BinaryOp.class).orElseThrow();
      var lhs = NumericUtils.getNumber(state, binOp.getLhs());
      var rhs = NumericUtils.getNumber(state, binOp.getRhs());
      var lhsType = binOp.getLhs().getType();
      var rhsType = binOp.getRhs().getType();
      var resultType = binOp.getResultType();

      var result = binaryOperation(lhs, rhs, lhsType, rhsType, resultType, binOp.getMode());
      state.setValueForOutput(op, result);
      return Action.Next();
    }

    /** Central dispatcher for binary arithmetic execution. */
    static @NotNull Number binaryOperation(
        @NotNull Number lhs,
        @NotNull Number rhs,
        @NotNull Type lhsType,
        @NotNull Type rhsType,
        @NotNull Type resultType,
        @NotNull ArithAttrs.BinModeAttr.BinMode binMode) {
      return switch (binMode) {
        case LT, LE, GT, GE, EQ, NE ->
            compareOperation(lhs, rhs, lhsType, rhsType, resultType, binMode);
        case AND, OR, XOR -> logicalOperation(lhs, rhs, resultType, binMode);
        case DIVUI, MODUI -> unsignedIntegerOperation(lhs, rhs, resultType, binMode);
        case ADD, SUB, MUL, DIV, MOD, BOR, BAND, BXOR, LSH, RSHS, RSHU ->
            numericOrIntegerOperation(lhs, rhs, resultType, binMode);
      };
    }

    /**
     * Executes comparison modes ({@code LT, LE, GT, GE, EQ, NE}).
     *
     * <p>If either operand is float, compare in floating-point domain. Otherwise compare integers
     * using signed or unsigned rules chosen from the dominant integer type.
     */
    private static @NotNull Number compareOperation(
        @NotNull Number lhs,
        @NotNull Number rhs,
        @NotNull Type lhsType,
        @NotNull Type rhsType,
        @NotNull Type resultType,
        @NotNull ArithAttrs.BinModeAttr.BinMode binMode) {
      if (!(resultType instanceof BuiltinTypes.IntegerT boolType)
          || !boolType.equals(BuiltinTypes.IntegerT.BOOL)) {
        throw new IllegalArgumentException("Comparison result type must be bool: " + resultType);
      }

      int comparison;
      if (lhsType instanceof BuiltinTypes.FloatT || rhsType instanceof BuiltinTypes.FloatT) {
        comparison = Double.compare(lhs.doubleValue(), rhs.doubleValue());
      } else {
        BuiltinTypes.IntegerT dominantType =
            (BuiltinTypes.IntegerT) BuiltinTypes.getDominantType(lhsType, rhsType);
        // Keep only the active bit-width before comparing unsigned values.
        long left = dominantType.normalizedLongRepresentation(lhs);
        long right = dominantType.normalizedLongRepresentation(rhs);
        comparison =
            dominantType.isSigned() ? Long.compare(left, right) : Long.compareUnsigned(left, right);
      }
      boolean result =
          switch (binMode) {
            case LT -> comparison < 0;
            case LE -> comparison <= 0;
            case GT -> comparison > 0;
            case GE -> comparison >= 0;
            case EQ -> comparison == 0;
            case NE -> comparison != 0;
            default ->
                throw new IllegalArgumentException("Unsupported comparison operation: " + binMode);
          };
      return boolType.convertToValidNumber(result ? 1 : 0);
    }

    /**
     * Executes boolean logic modes ({@code AND, OR, XOR}) on integer-backed booleans.
     *
     * <p>Non-zero is treated as true, zero as false, and the result is normalized back into the
     * configured integer bool representation.
     */
    private static @NotNull Number logicalOperation(
        @NotNull Number lhs,
        @NotNull Number rhs,
        @NotNull Type resultType,
        @NotNull ArithAttrs.BinModeAttr.BinMode binMode) {
      if (!(resultType instanceof BuiltinTypes.IntegerT integerT)
          || !integerT.equals(BuiltinTypes.IntegerT.BOOL)) {
        throw new IllegalArgumentException(
            "Logical operation result type must be bool: " + resultType);
      }
      boolean left = lhs.longValue() != 0;
      boolean right = rhs.longValue() != 0;
      long result =
          switch (binMode) {
            case AND -> left && right ? 1 : 0;
            case OR -> left || right ? 1 : 0;
            case XOR -> left ^ right ? 1 : 0;
            default ->
                throw new IllegalArgumentException("Unsupported logical operation: " + binMode);
          };
      return integerT.convertToValidNumber(result);
    }

    /**
     * Executes unsigned arithmetic modes ({@code DIVUI, MODUI}).
     *
     * <p>Operands are first masked to the target integer width so Java signed storage still behaves
     * as the intended unsigned bit-pattern.
     */
    private static @NotNull Number unsignedIntegerOperation(
        @NotNull Number lhs,
        @NotNull Number rhs,
        @NotNull Type resultType,
        @NotNull ArithAttrs.BinModeAttr.BinMode binMode) {
      if (!(resultType instanceof BuiltinTypes.IntegerT integerT)) {
        throw new IllegalArgumentException(
            "Unsigned integer operation requires an integer result type: " + resultType);
      }
      long left = integerT.normalizedLongRepresentation(lhs);
      long right = integerT.normalizedLongRepresentation(rhs);
      long result =
          switch (binMode) {
            case DIVUI -> Long.divideUnsigned(left, right);
            case MODUI -> Long.remainderUnsigned(left, right);
            default ->
                throw new IllegalArgumentException("Unsupported unsigned operation: " + binMode);
          };
      return integerT.convertToValidNumber(result);
    }

    /**
     * Executes regular arithmetic, bitwise, and shift modes.
     *
     * <p>Float result types use float/double arithmetic. Integer result types use integer
     * arithmetic and bit operations, with final narrowing via {@link
     * BuiltinTypes.IntegerT#convertToValidNumber(long)}.
     */
    private static @NotNull Number numericOrIntegerOperation(
        @NotNull Number lhs,
        @NotNull Number rhs,
        @NotNull Type resultType,
        @NotNull ArithAttrs.BinModeAttr.BinMode binMode) {
      if (resultType instanceof BuiltinTypes.FloatT floatT) {
        if (floatT.getWidth() == 32) {
          float left = lhs.floatValue();
          float right = rhs.floatValue();
          return switch (binMode) {
            case ADD -> left + right;
            case SUB -> left - right;
            case MUL -> left * right;
            case DIV -> left / right;
            case MOD -> left % right;
            default ->
                throw new IllegalArgumentException("Unsupported float operation: " + binMode);
          };
        }
        double left = lhs.doubleValue();
        double right = rhs.doubleValue();
        return switch (binMode) {
          case ADD -> left + right;
          case SUB -> left - right;
          case MUL -> left * right;
          case DIV -> left / right;
          case MOD -> left % right;
          default -> throw new IllegalArgumentException("Unsupported double operation: " + binMode);
        };
      }

      if (resultType instanceof BuiltinTypes.IntegerT integerT) {
        // Mask before bit operations so the operation respects the declared integer width.
        long left = integerT.normalizedLongRepresentation(lhs);
        long right = integerT.normalizedLongRepresentation(rhs);
        int shiftAmount = (int) rhs.longValue();
        long result =
            switch (binMode) {
              case ADD -> left + right;
              case SUB -> left - right;
              case MUL -> left * right;
              case DIV -> left / right;
              case MOD -> left % right;
              case BOR -> left | right;
              case BAND -> left & right;
              case BXOR -> left ^ right;
              case LSH -> left << shiftAmount;
              case RSHS -> left >> shiftAmount;
              // Unsigned right-shift must not sign-extend the high bit.
              case RSHU -> left >>> shiftAmount;
              default ->
                  throw new IllegalArgumentException("Unsupported integer operation: " + binMode);
            };
        return integerT.convertToValidNumber(result);
      }

      throw new IllegalArgumentException(
          "Unsupported numeric result type: " + resultType + " for mode " + binMode);
    }
  }

  final class CastRunner extends OpRunner implements ArithRunners {
    public CastRunner() {
      super(ArithOps.CastOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      ArithOps.CastOp castOp = op.as(ArithOps.CastOp.class).orElseThrow();
      var value = NumericUtils.getNumber(state, castOp.getOperand());
      var targetType = castOp.getTargetType();
      var converted = NumericUtils.convertToType(value, targetType);
      state.setValueForOutput(op, converted);
      return Action.Next();
    }
  }

  final class ConstantRunner extends OpRunner implements ArithRunners {
    public ConstantRunner() {
      super(ArithOps.ConstantOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      ArithOps.ConstantOp constantOp = op.as(ArithOps.ConstantOp.class).orElseThrow();
      state.setValue(constantOp.getValue(), constantOp.getValueStorage());
      return Action.Next();
    }
  }
}

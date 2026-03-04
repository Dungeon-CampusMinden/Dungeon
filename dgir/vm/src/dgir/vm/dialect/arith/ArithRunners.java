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
  final class BinaryRunner extends OpRunner implements ArithRunners {
    public BinaryRunner() {
      super(ArithOps.BinaryOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      ArithOps.BinaryOp binOp = op.as(ArithOps.BinaryOp.class).orElseThrow();
      var lhs = NumericUtils.getNumber(state, binOp.getLhs());
      var rhs = NumericUtils.getNumber(state, binOp.getRhs());
      var resultType = binOp.getResultType();

      var result = binaryOperation(lhs, rhs, resultType, binOp.getMode());
      state.setValueForOutput(op, result);
      return Action.Next();
    }

    /**
     * Perform binary numeric operations according to the result type. If the result type is a
     * float, both operands are treated as floats (promoting to double if necessary). If the result
     * type is an integer, both operands are treated as longs and the result is converted back to
     * the appropriate integer type. Supported operations are addition, subtraction, multiplication,
     * division, and modulus.
     */
    static @NotNull Number binaryOperation(
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
            case LT -> left < right ? 1 : 0;
            case LE -> left <= right ? 1 : 0;
            case GT -> left > right ? 1 : 0;
            case GE -> left >= right ? 1 : 0;
            case EQ -> left == right ? 1 : 0;
            case NE -> left != right ? 1 : 0;
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
          case LT -> left < right ? 1 : 0;
          case LE -> left <= right ? 1 : 0;
          case GT -> left > right ? 1 : 0;
          case GE -> left >= right ? 1 : 0;
          case EQ -> left == right ? 1 : 0;
          case NE -> left != right ? 1 : 0;
          default -> throw new IllegalArgumentException("Unsupported double operation: " + binMode);
        };
      }

      if (resultType instanceof BuiltinTypes.IntegerT integerT) {
        long left = lhs.longValue();
        long right = rhs.longValue();
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
              case LSH -> left << right;
              case RSHS -> left >> right;
              case RSHU -> left >>> right;
              case AND -> (left != 0) && (right != 0) ? 1 : 0;
              case OR -> (left != 0) || (right != 0) ? 1 : 0;
              case XOR -> ((left != 0) ^ (right != 0)) ? 1 : 0;
              case LT -> left < right ? 1 : 0;
              case LE -> left <= right ? 1 : 0;
              case GT -> left > right ? 1 : 0;
              case GE -> left >= right ? 1 : 0;
              case EQ -> left == right ? 1 : 0;
              case NE -> left != right ? 1 : 0;
              default ->
                  throw new IllegalArgumentException("Unsupported integer operation: " + binMode);
            };
        return integerT.convertToValidNumber(result);
      }

      throw new IllegalArgumentException("Unsupported numeric result type: " + resultType);
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

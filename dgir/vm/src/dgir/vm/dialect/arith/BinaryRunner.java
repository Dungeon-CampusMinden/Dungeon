package dgir.vm.dialect.arith;

import static dialect.arith.ArithAttrs.BinModeAttr.BinMode;
import static dialect.arith.ArithOps.BinaryOp;
import static dialect.builtin.BuiltinTypes.FloatT;
import static dialect.builtin.BuiltinTypes.IntegerT;

import core.ir.Operation;
import core.ir.Type;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

public class BinaryRunner extends OpRunner {
  public BinaryRunner() {
    super(BinaryOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    BinaryOp binOp = op.as(BinaryOp.class).orElseThrow();
    var lhs = NumericUtils.getNumber(state, binOp.getLhs());
    var rhs = NumericUtils.getNumber(state, binOp.getRhs());
    var resultType = binOp.getResultType();

    var result = binaryOperation(lhs, rhs, resultType, binOp.getMode());
    state.setValueForOutput(op, result);
    return Action.Next();
  }

  /**
   * Perform binary numeric operations according to the result type. If the result type is a float,
   * both operands are treated as floats (promoting to double if necessary). If the result type is
   * an integer, both operands are treated as longs and the result is converted back to the
   * appropriate integer type. Supported operations are addition, subtraction, multiplication,
   * division, and modulus.
   */
  static @NotNull Number binaryOperation(
      @NotNull Number lhs,
      @NotNull Number rhs,
      @NotNull Type resultType,
      @NotNull BinMode binMode) {
    if (resultType instanceof FloatT floatT) {
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
          default -> throw new IllegalArgumentException("Unsupported float operation: " + binMode);
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

    if (resultType instanceof IntegerT integerT) {
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

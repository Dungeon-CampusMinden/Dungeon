package dgir.vm.dialect.arith;

import core.ir.Operation;
import core.ir.Type;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.arith.BinaryOp;
import dialect.arith.attributes.BinModeAttr;
import dialect.builtin.types.FloatT;
import dialect.builtin.types.IntegerT;
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

    var result = binaryNumeric(lhs, rhs, resultType, binOp.getBinMode());
    state.setValueForOutput(op, result);
    return Action.Next();
  }

  /**
   * Perform binary numeric operations according to the result type. If the result type is a float,
   * both operands are treated as floats (promoting to double if necessary). If the result type is
   * an integer, both operands are treated as longs and the result is converted back to the
   * appropriate integer type. Supported operations are addition, subtraction, multiplication,
   * division, and modulus.
   *
   */
  static @NotNull Number binaryNumeric(
    @NotNull Number lhs,
    @NotNull Number rhs,
    @NotNull Type resultType,
    @NotNull BinModeAttr.BinMode mode) {
    if (resultType instanceof FloatT floatT) {
      if (floatT.getWidth() == 32) {
        float left = lhs.floatValue();
        float right = rhs.floatValue();
        return switch (mode) {
          case ADD -> left + right;
          case SUB -> left - right;
          case MUL -> left * right;
          case DIV -> left / right;
          case MOD -> left % right;
        };
      }
      double left = lhs.doubleValue();
      double right = rhs.doubleValue();
      return switch (mode) {
        case ADD -> left + right;
        case SUB -> left - right;
        case MUL -> left * right;
        case DIV -> left / right;
        case MOD -> left % right;
      };
    }

    if (resultType instanceof IntegerT integerT) {
      long left = lhs.longValue();
      long right = rhs.longValue();
      long result =
        switch (mode) {
          case ADD -> left + right;
          case SUB -> left - right;
          case MUL -> left * right;
          case DIV -> left / right;
          case MOD -> left % right;
        };
      return integerT.convertToValidNumber(result);
    }

    throw new IllegalArgumentException("Unsupported numeric result type: " + resultType);
  }
}


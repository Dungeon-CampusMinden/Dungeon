package dgir.vm.dialect.arith;

import dgir.core.ir.Type;
import dgir.core.ir.Value;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import static dgir.dialect.builtin.BuiltinTypes.FloatT;
import static dgir.dialect.builtin.BuiltinTypes.IntegerT;

final class NumericUtils {
  private NumericUtils() {}

  /**
   * Retrieve a numeric value from the state for the given operand. The value must be an instance of
   * Number (e.g., Integer, Long, Float, Double). If the value is not numeric, an
   * IllegalStateException is thrown.
   *
   * @param state the state
   * @param value the operand
   * @return the numeric value
   * @throws IllegalStateException if the value is not numeric
   */
  static @NotNull Number getNumber(@NotNull State state, @NotNull Value value) {
    Object obj = state.getValue(value);
    if (!(obj instanceof Number number)) {
      throw new IllegalStateException("Operand value must be numeric: " + obj);
    }
    return number;
  }

  /**
   * Convert a numeric value to the specified target type. Supported target types are IntegerT and
   * FloatT. For IntegerT, the value is converted to a long and then back to the appropriate integer
   * type using IntegerT's conversion method. For FloatT, the value is converted to either float or
   * double based on the width of the FloatT. If the target type is not supported, an
   * IllegalArgumentException is thrown.
   *
   * @param value the numeric value to convert
   * @param targetType the target type
   * @return the converted value
   */
  static @NotNull Number convertToType(@NotNull Number value, @NotNull Type targetType) {
    if (targetType instanceof IntegerT integerT) {
      return integerT.convertToValidNumber(value);
    }
    if (targetType instanceof FloatT floatT) {
      return floatT.convertToValidNumber(value);
    }
    throw new IllegalArgumentException("Unsupported numeric target type: " + targetType);
  }
}

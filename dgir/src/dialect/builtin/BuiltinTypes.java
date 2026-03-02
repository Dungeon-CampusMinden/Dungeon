package dialect.builtin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.Dialect;
import core.ir.Type;
import java.util.List;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public sealed interface BuiltinTypes {
  /**
   * Abstract base class for all types contributed by the {@link BuiltinDialect}.
   *
   * <p>Subclasses must implement {@link #getIdent()}, {@link #getValidator()}, and, for
   * parameterized types, {@link #getParameterizedIdent()} and {@link
   * #getParameterizedStringFactory()}.
   */
  abstract class BuiltinType extends Type {
    @Override
    public @NotNull String getNamespace() {
      return "";
    }

    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return BuiltinDialect.class;
    }
  }

  /**
   * Fixed-width integer type in the {@code builtin} dialect.
   *
   * <p>Supported widths: {@code 1} (bool), {@code 8}, {@code 16}, {@code 32}, {@code 64}.
   *
   * <p>Canonical ident: {@code int} (the width is not part of the ident — instances are compared by
   * parameterized ident, e.g. {@code int<32>}).
   *
   * <p>Pre-built singleton instances are available as static constants:
   *
   * <pre>
   *   IntegerT.BOOL / IntegerT.INT1  — 1-bit boolean
   *   IntegerT.INT8                  — 8-bit signed integer
   *   IntegerT.INT16                 — 16-bit signed integer
   *   IntegerT.INT32                 — 32-bit signed integer
   *   IntegerT.INT64                 — 64-bit signed integer
   * </pre>
   */
  final class IntegerT extends BuiltinType implements BuiltinTypes {

    // =========================================================================
    // Static Fields
    // =========================================================================

    /** 1-bit integer used as a boolean ({@code false} = 0, {@code true} = 1). */
    public static final IntegerT INT1 = new IntegerT(1);

    /** Alias for {@link #INT1}. */
    public static final IntegerT BOOL = INT1;

    /** 8-bit signed integer. */
    public static final IntegerT INT8 = new IntegerT(8);

    /** 16-bit signed integer. */
    public static final IntegerT INT16 = new IntegerT(16);

    /** 32-bit signed integer. */
    public static final IntegerT INT32 = new IntegerT(32);

    /** 64-bit signed integer. */
    public static final IntegerT INT64 = new IntegerT(64);

    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    public @NotNull String getIdent() {
      return "int" + getWidth();
    }

    @Override
    public Function<Object, Boolean> getValidator() {
      return value -> {
        if (!(value instanceof Number number)) return false;

        switch (number) {
          case Byte ignored when getWidth() == 1 || getWidth() == 8 -> {
            return true;
          }
          case Short ignored when getWidth() == 16 -> {
            return true;
          }
          case Integer ignored when getWidth() == 32 -> {
            return true;
          }
          case Long ignored when getWidth() == 64 -> {
            return true;
          }
          default -> {
            return false;
          }
        }
      };
    }

    @Override
    public @NotNull @Unmodifiable List<Type> getDefaultTypeInstances() {
      return List.of(INT1, INT8, INT16, INT32, INT64);
    }

    // =========================================================================
    // Members
    // =========================================================================

    /** The bit-width of this integer type (1, 8, 16, 32, or 64). */
    private final int width;

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Create a default 32-bit integer type. */
    public IntegerT() {
      width = 32;
    }

    /**
     * Create an integer type with the given bit-width.
     *
     * @param width must be one of 1, 8, 16, 32, or 64.
     */
    public IntegerT(int width) {
      assert width == 1 || width == 8 || width == 16 || width == 32 || width == 64
          : "Invalid integer width: " + width;
      this.width = width;
    }

    // =========================================================================
    // Functions
    // =========================================================================

    /**
     * Returns the bit-width of this integer type.
     *
     * @return the bit-width (1, 8, 16, 32, or 64).
     */
    public int getWidth() {
      return width;
    }

    /**
     * Take a number of any integer type and convert it to the correct Java type for this {@code
     * IntegerT}. For example, if this is {@link #INT16} and the input is a {@code Byte}, it is
     * widened to a {@code Short}.
     *
     * @param number the number to convert; must not be a floating-point value.
     * @return the converted number in the narrowest Java type that matches this width.
     * @throws IllegalArgumentException if {@code number} is a float or double.
     */
    public Number convertToValidNumber(Number number) {
      if (number instanceof Float || number instanceof Double) {
        throw new IllegalArgumentException(
            "Cannot convert floating point number to integer: " + number);
      }

      return switch (getWidth()) {
        case 1 -> (byte) (number.intValue() == 0 ? 0 : 1); // Mask to 1 bit for boolean
        case 8 -> number.byteValue();
        case 16 -> number.shortValue();
        case 32 -> number.intValue();
        case 64 -> number.longValue();
        default -> throw new RuntimeException("Invalid integer width: " + getWidth());
      };
    }
  }

  /**
   * Floating-point type in the {@code builtin} dialect.
   *
   * <p>Supported widths: {@code 32} (single-precision) and {@code 64} (double-precision).
   *
   * <p>Pre-built singleton instances:
   *
   * <pre>
   *   FloatT.FLOAT32  — 32-bit IEEE 754 float
   *   FloatT.FLOAT64  — 64-bit IEEE 754 double
   * </pre>
   */
  final class FloatT extends BuiltinType implements BuiltinTypes {

    // =========================================================================
    // Static Fields
    // =========================================================================

    /** 32-bit single-precision floating-point type. */
    public static final FloatT FLOAT32 = new FloatT(32);

    /** 64-bit double-precision floating-point type. */
    public static final FloatT FLOAT64 = new FloatT(64);

    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    public @NotNull String getIdent() {
      return "float" + getWidth();
    }

    @Override
    public Function<Object, Boolean> getValidator() {
      return value -> {
        if (!(value instanceof Number)) return false;

        return switch (value) {
          case Float ignored when getWidth() == 32 -> true;
          case Double ignored when getWidth() == 64 -> true;
          default -> false;
        };
      };
    }

    @Override
    public @NotNull @Unmodifiable List<Type> getDefaultTypeInstances() {
      return List.of(FLOAT32, FLOAT64);
    }

    // =========================================================================
    // Members
    // =========================================================================

    /** The bit-width of this floating-point type (32 or 64). */
    private final int width;

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Create a default 32-bit float type. */
    public FloatT() {
      width = 32;
    }

    /**
     * Create a floating-point type with the given bit-width.
     *
     * @param width must be either 32 or 64.
     */
    public FloatT(int width) {
      assert width == 32 || width == 64 : "Invalid float width: " + width;
      this.width = width;
    }

    // =========================================================================
    // Functions
    // =========================================================================

    /**
     * Returns the bit-width of this floating-point type.
     *
     * @return the bit-width (32 or 64).
     */
    @JsonIgnore
    public int getWidth() {
      return width;
    }

    public Number convertToValidNumber(Number number) {
      if (number instanceof Float || number instanceof Double) {
        return switch (getWidth()) {
          case 32 -> number.floatValue();
          case 64 -> number.doubleValue();
          default -> throw new RuntimeException("Invalid float width: " + getWidth());
        };
      }
      throw new IllegalArgumentException(
          "Cannot convert floating point number to float: " + number);
    }
  }

  /**
   * UTF-16 string type in the {@code builtin} dialect.
   *
   * <p>Ident: {@code string}. Validated values must be Java {@link String} instances.
   *
   * <p>The single pre-built instance is available as {@link #INSTANCE}.
   */
  final class StringT extends BuiltinType implements BuiltinTypes {

    // =========================================================================
    // Static Fields
    // =========================================================================

    /** Singleton instance of the string type. */
    public static final StringT INSTANCE = new StringT();

    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    public @NotNull String getIdent() {
      return "string";
    }

    @Override
    public Function<Object, Boolean> getValidator() {
      return value -> value instanceof String;
    }

    @Override
    public @NotNull @Unmodifiable List<Type> getDefaultTypeInstances() {
      return List.of(INSTANCE);
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Creates a new {@code StringT} instance. Prefer {@link #INSTANCE} over this constructor. */
    public StringT() {}
  }
}

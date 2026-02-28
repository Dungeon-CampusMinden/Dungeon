package dialect.builtin.types;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

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
public class IntegerT extends BuiltinType {

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

    long value = number.longValue();
    switch (getWidth()) {
      case 1 -> {
        return (byte) (value & 0x1L);
      }
      case 8 -> {
        return (byte) value;
      }
      case 16 -> {
        return (short) value;
      }
      case 32 -> {
        return (int) value;
      }
      case 64 -> {
        return value;
      }
      default -> throw new RuntimeException("Invalid integer width: " + getWidth());
    }
  }
}

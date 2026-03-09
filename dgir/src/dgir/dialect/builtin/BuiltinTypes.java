package dgir.dialect.builtin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dgir.core.Dialect;
import dgir.core.ir.Type;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Function;

public sealed interface BuiltinTypes {
  static boolean isNumeric(@NotNull Type type) {
    return type instanceof IntegerT || type instanceof FloatT;
  }

  static @NotNull Type getDominantType(@NotNull Type lhsType, @NotNull Type rhsType) {
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
    boolean lhsIsSigned = ((IntegerT) lhsType).isSigned();
    int rhsWidth = ((IntegerT) rhsType).getWidth();
    boolean rhsIsSigned = ((IntegerT) rhsType).isSigned();
    // By default, the result is signed if both operands are signed. However, if one operand is
    // wider than the other, we take the signedness of the wider operand. This allows operations
    // like int8 + uint32 to yield a uint32 result, which is more intuitive and prevents accidental
    // overflow.
    boolean shouldBeSigned = lhsIsSigned && rhsIsSigned;
    if (lhsIsSigned != rhsIsSigned) {
      if (lhsWidth > rhsWidth) {
        shouldBeSigned = lhsIsSigned;
      } else {
        shouldBeSigned = rhsIsSigned;
      }
    }
    return integerTypeByWidth(Math.max(lhsWidth, rhsWidth), shouldBeSigned);
  }

  static @NotNull IntegerT integerTypeByWidth(int width, boolean isSigned) {
    return switch (width) {
      case 1 -> IntegerT.INT1;
      case 8 -> isSigned ? IntegerT.INT8 : IntegerT.UINT8;
      case 16 -> isSigned ? IntegerT.INT16 : IntegerT.UINT16;
      case 32 -> isSigned ? IntegerT.INT32 : IntegerT.UINT32;
      case 64 -> isSigned ? IntegerT.INT64 : IntegerT.UINT64;
      default -> throw new IllegalArgumentException("Invalid integer width: " + width);
    };
  }

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
   *   IntegerT.UINT8                 — 8-bit unsigned integer
   *   IntegerT.UINT16                — 16-bit unsigned integer
   *   IntegerT.UINT32                — 32-bit unsigned integer
   *   IntegerT.UINT64                — 64-bit unsigned integer
   * </pre>
   */
  final class IntegerT extends BuiltinType implements BuiltinTypes {

    // =========================================================================
    // Static Fields
    // =========================================================================

    /** 1-bit integer used as a boolean ({@code false} = 0, {@code true} = 1). */
    public static final IntegerT INT1 = new IntegerT(1, true);

    /** Alias for {@link #INT1}. */
    public static final IntegerT BOOL = INT1;

    /** 8-bit signed integer. */
    public static final IntegerT INT8 = new IntegerT(8, true);

    /** 16-bit signed integer. */
    public static final IntegerT INT16 = new IntegerT(16, true);

    /** 32-bit signed integer. */
    public static final IntegerT INT32 = new IntegerT(32, true);

    /** 64-bit signed integer. */
    public static final IntegerT INT64 = new IntegerT(64, true);

    /** 8-bit unsigned integer. */
    public static final IntegerT UINT8 = new IntegerT(8, false);

    /** 16-bit unsigned integer. */
    public static final IntegerT UINT16 = new IntegerT(16, false);

    /** 32-bit unsigned integer. */
    public static final IntegerT UINT32 = new IntegerT(32, false);

    /** 64-bit unsigned integer. */
    public static final IntegerT UINT64 = new IntegerT(64, false);

    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    public @NotNull String getIdent() {
      return (isSigned() ? "" : "u") + "int" + getWidth();
    }

    /**
     * Equality is based on the parameterized ident, ignoring the "u" prefix for unsigned types.
     * This allows signed and unsigned types of the same width to be considered equal for type
     * checking purposes, since the signedness is mostly a semantic detail that is important for the
     * arithmetic operations.
     */
    public boolean equal(@Nullable Object obj) {
      if (obj instanceof Type other) {
        String normalizedOther = other.getParameterizedIdent().replace("u", "");
        String normalizedThis = getParameterizedIdent().replace("u", "");
        return normalizedThis.equals(normalizedOther);
      }
      return false;
    }

    @Override
    public Function<Object, Boolean> getValidator() {
      return value -> {
        if (!(value instanceof Number number)) return false;

        return switch (number) {
          case Byte ignored when getWidth() == 1 || getWidth() == 8 -> true;
          case Short ignored when getWidth() == 16 -> true;
          case Integer ignored when getWidth() == 32 -> true;
          case Long ignored when getWidth() == 64 -> true;
          default -> false;
        };
      };
    }

    @Override
    public @NotNull @Unmodifiable List<Type> getDefaultTypeInstances() {
      return List.of(INT1, INT8, INT16, INT32, INT64, UINT8, UINT16, UINT32, UINT64);
    }

    // =========================================================================
    // Members
    // =========================================================================

    /** The bit-width of this integer type (1, 8, 16, 32, or 64). */
    private final int width;

    /** Whether this type is signed. */
    private final boolean signed;

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Create a default 32-bit integer type. */
    IntegerT() {
      this(32);
    }

    /**
     * Create a signed integer type with the given bit-width.
     *
     * @param width must be one of 1, 8, 16, 32, or 64.
     */
    private IntegerT(int width) {
      this(width, true);
    }

    /**
     * Create an integer type with the given bit-width and signedness.
     *
     * @param width must be one of 1, 8, 16, 32, or 64.
     * @param isSigned whether this type is signed.
     */
    private IntegerT(int width, boolean isSigned) {
      assert width == 1 || width == 8 || width == 16 || width == 32 || width == 64
          : "Invalid integer width: " + width;
      this.width = width;
      this.signed = isSigned;
    }

    // =========================================================================
    // Functions
    // =========================================================================

    /**
     * Returns the bit-width of this integer type.
     *
     * @return the bit-width (1, 8, 16, 32, or 64).
     */
    @Contract(pure = true)
    public int getWidth() {
      return width;
    }

    /**
     * Returns whether this integer type is signed.
     *
     * @return {@code true} if this type is signed, {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isSigned() {
      return signed;
    }

    /**
     * Take a number of any integer type and convert it to the correct Java type for this {@code
     * IntegerT}. For example, if this is {@link #INT16} and the input is a {@code Byte}, it is
     * widened to a {@code Short}.
     *
     * <p>For the 1-bit boolean type, any nonzero input is converted to 1, and zero is converted to
     * 0.
     *
     * <p>Signedness is implicitly handled. If you want to store a value of 255 in a byte, just pass
     * 255 to the function. The conversion to byte will cause the "signed" value to be -1, which is
     * the correct two's complement representation of 255 in a byte. During execution, it is the
     * responsibility of the runtime to call the correctly signed operations.
     *
     * <p>If you want to assign large unsigned values to long variables, you can use {@code UINT64}
     * and pass in a {@code Long} value. The conversion will not change the bits, so a value like
     * 2^63 will be represented as -2^63 in the resulting {@code Long}. Again, it is the
     * responsibility of the runtime to handle this correctly. {@code 0xFFFFFFFFFFFFFFFFL} is the
     * largest value that can be represented in an {@code Unsigned Long}.
     *
     * @param number the number to convert
     * @return the converted number in the narrowest Java type that matches this width.
     */
    @Contract(pure = true)
    public Number convertToValidNumber(long number) {
      return switch (width) {
        case 1 -> (byte) (number == 0 ? 0 : 1);
        case 8 -> (byte) number;
        case 16 -> (short) number;
        case 32 -> (int) number;
        case 64 -> number;
        default -> throw new RuntimeException("Invalid integer width: " + width);
      };
    }

    /**
     * For a given number, return its normalized long representation according to this integer type.
     * For signed types, this is just the long value of the number. For unsigned types, this is the
     * long value masked to the appropriate number of bits. For example, if this is {@code uint8}
     * and the input number is -1 (which would be 0xFFFFFFFFFFFFFFFF in two's complement), the
     * normalized long representation would be 255 (0xFF), which is the correct unsigned
     * interpretation of the bits.
     *
     * @param number the number to normalize
     * @return the normalized long representation of the number.
     */
    public long normalizedLongRepresentation(long number) {
      if (isSigned()) {
        return number;
      } else {
        return number & (1L << width) - 1L;
      }
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
   *   FloatT.FLOAT32 — 32-bit IEEE 754 float
   *   FloatT.FLOAT64 — 64-bit IEEE 754 double
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

    private final @NotNull Function<@NotNull Number, @NotNull Number> conversionFunction;

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Create a default 32-bit float type. */
    FloatT() {
      this(32);
    }

    /**
     * Create a floating-point type with the given bit-width.
     *
     * @param width must be either 32 or 64.
     */
    private FloatT(int width) {
      assert width == 32 || width == 64 : "Invalid float width: " + width;
      this.width = width;
      conversionFunction = pickCorrectConversion(width);
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
      return conversionFunction.apply(number);
    }

    private static @NotNull Function<@NotNull Number, @NotNull Number> pickCorrectConversion(
        int width) {
      return switch (width) {
        case 32 -> Number::floatValue;
        case 64 -> Number::doubleValue;
        default -> throw new RuntimeException("Invalid float width: " + width);
      };
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
    StringT() {}
  }
}

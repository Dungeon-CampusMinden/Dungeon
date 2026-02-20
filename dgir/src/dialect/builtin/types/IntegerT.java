package dialect.builtin.types;

import core.Dialect;
import core.ir.Type;
import core.detail.TypeDetails;
import dialect.builtin.Builtin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IntegerT extends Type {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final IntegerT INT1  = new IntegerT(1);
  public static final IntegerT BOOL  = INT1;
  public static final IntegerT INT8  = new IntegerT(8);
  public static final IntegerT INT16 = new IntegerT(16);
  public static final IntegerT INT32 = new IntegerT(32);
  public static final IntegerT INT64 = new IntegerT(64);

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public TypeDetails.@NotNull Impl createImpl() {
    class IntegerTModel extends TypeDetails.Impl {
      IntegerTModel(Type defaultInstance, String name) {
        super(defaultInstance, name, IntegerT.class, Dialect.getOrThrow(Builtin.class));
      }
    }
    return new IntegerTModel(this, getIdent() + getWidth());
  }

  public static String getIdent() {
    return "int";
  }

  public static String getNamespace() {
    return "";
  }

  // =========================================================================
  // Members
  // =========================================================================

  private final int width;

  // =========================================================================
  // Constructors
  // =========================================================================

  public IntegerT() {
    width = 32;
  }

  public IntegerT(int width) {
    assert width == 1 || width == 8 || width == 16 || width == 32 || width == 64 : "Invalid integer width: " + width;
    this.width = width;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public int getWidth() {
    return width;
  }

  @Override
  public boolean validate(@Nullable Object value) {
    if (!(value instanceof Number number))
      return false;

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
  }

  /**
   * Take a number of any integer type and convert it to the correct type for this IntegerT. For example, if this is an
   * INT16 and the input is a Byte, convert it to a Short.
   *
   * @param number The number to convert.
   * @return The converted number.
   */
  public Number convertToValidNumber(Number number) {
    if (number instanceof Float || number instanceof Double) {
      throw new IllegalArgumentException("Cannot convert floating point number to integer: " + number);
    }

    long value = number.longValue();
    switch (getWidth()) {
      case 1 -> {
        assert value == 0 || value == 1 : "Value out of range: " + value + " for width: " + getWidth();
        return (byte) value;
      }
      case 8 -> {
        assert value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE : "Value out of range: " + value + " for width: " + getWidth();
        return (byte) value;
      }
      case 16 -> {
        assert value >= Short.MIN_VALUE && value <= Short.MAX_VALUE : "Value out of range: " + value + " for width: " + getWidth();
        return (short) value;
      }
      case 32 -> {
        assert value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE : "Value out of range: " + value + " for width: " + getWidth();
        return (int) value;
      }
      case 64 -> {
        return value;
      }
      default -> throw new RuntimeException("Invalid integer width: " + getWidth());
    }
  }
}

package dialect.builtin.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

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
public class FloatT extends BuiltinType {

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

      switch (value) {
        case Float ignored when getWidth() == 32 -> {
          return true;
        }
        case Double ignored when getWidth() == 64 -> {
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
}

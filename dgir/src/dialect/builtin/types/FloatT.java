package dialect.builtin.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import core.Dialect;
import core.detail.TypeDetails;
import core.ir.Type;
import dialect.builtin.Builtin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FloatT extends Type {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final FloatT FLOAT32 = new FloatT(32);
  public static final FloatT FLOAT64 = new FloatT(64);

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public TypeDetails.@NotNull Impl createImpl() {
    class FloatTModel extends TypeDetails.Impl {
      FloatTModel(Type defaultInstance, int width) {
        super(
            defaultInstance,
            FloatT.getIdent() + width,
            FloatT.class,
            Dialect.getOrThrow(Builtin.class));
      }
    }
    return new FloatTModel(this, getWidth());
  }

  public static String getIdent() {
    return "float";
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

  public FloatT() {
    width = 32;
  }

  public FloatT(int width) {
    assert width == 32 || width == 64 : "Invalid float width: " + width;
    this.width = width;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @JsonIgnore
  public int getWidth() {
    return width;
  }

  @Override
  public boolean validate(@Nullable Object value) {
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
  }
}

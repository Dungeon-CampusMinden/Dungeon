package dialect.builtin.types;

import core.Dialect;
import core.ir.Type;
import core.detail.TypeDetails;
import dialect.builtin.Builtin;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
  public TypeDetails.Impl createImpl() {
    class FloatTModel extends TypeDetails.Impl {
      FloatTModel(Type defaultInstance, int width) {
        super(defaultInstance, FloatT.getIdent() + width, FloatT.class, Dialect.get(Builtin.class));
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
  public boolean validate(Object value) {
    if (!(value instanceof Number))
      return false;

    switch (value) {
      case Float f when getWidth() == 32 -> {
        return true;
      }
      case Double d when getWidth() == 64 -> {
        return true;
      }
      default -> {
        return false;
      }
    }
  }
}

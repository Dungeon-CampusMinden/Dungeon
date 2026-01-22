package blockly.vm.dgir.dialect.builtin.types;

import blockly.vm.dgir.core.ParametricType;
import blockly.vm.dgir.dialect.builtin.Builtin;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class FloatT extends ParametricType {
  public static final FloatT FLOAT32 = new FloatT(32);
  public static final FloatT FLOAT64 = new FloatT(64);

  public FloatT() {
    super(32);
  }

  public FloatT(int width) {
    super(width);
  }

  @JsonIgnore
  public int getWidth() {
    return (int) getParameters();
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

  @Override
  public String getIdent() {
    return "float" + getWidth();
  }

  @Override
  public String getNamespace() {
    return "";
  }
}

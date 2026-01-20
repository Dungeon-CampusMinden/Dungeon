package blockly.vm.dgir.dialect.builtin.types;

import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.dialect.builtin.Builtin;

public class Float32_t extends Type {
  public static final Float32_t INSTANCE = new Float32_t();

  public Float32_t() {
    super(Builtin.class, "float32");
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public boolean validate(Object value) {
    return value instanceof Float;
  }
}

package blockly.vm.dgir.dialect.builtin.types;

import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.dialect.builtin.Builtin;

public class Int32_t extends Type {
  public static final Int32_t INSTANCE = new Int32_t();

  public Int32_t() {
    super(Builtin.class, "int32");
  }
  @Override
  public Object getDefaultValue() {
    return 0;
  }


  @Override
  public boolean validate(Object value) {
    return value instanceof Integer;
  }
}

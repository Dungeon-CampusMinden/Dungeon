package blockly.vm.dgir.dialect.builtin.types;

import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.dialect.builtin.Builtin;

public class String_t extends Type {
  public static final String_t INSTANCE = new String_t();

  public String_t() {
    super(Builtin.class, "string");
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

  @Override
  public boolean validate(Object value) {
    return value instanceof String;
  }
}

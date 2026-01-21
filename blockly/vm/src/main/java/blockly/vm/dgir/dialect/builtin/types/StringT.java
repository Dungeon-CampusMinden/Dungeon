package blockly.vm.dgir.dialect.builtin.types;

import blockly.vm.dgir.core.type.PrimitiveType;
import blockly.vm.dgir.core.type.Type;
import blockly.vm.dgir.dialect.builtin.Builtin;

import java.util.List;

public class StringT extends PrimitiveType {
  public static final StringT INSTANCE = new StringT();

  public StringT() {
    super(Builtin.class, "string");
  }

  @Override
  public boolean validate(Object value) {
    return value instanceof String;
  }
}

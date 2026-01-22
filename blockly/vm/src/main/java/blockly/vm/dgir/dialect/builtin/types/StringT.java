package blockly.vm.dgir.dialect.builtin.types;

import blockly.vm.dgir.core.PrimitiveType;
import blockly.vm.dgir.dialect.builtin.Builtin;

public class StringT extends PrimitiveType {
  public static final StringT INSTANCE = new StringT();

  public StringT() {

  }

  @Override
  public boolean validate(Object value) {
    return value instanceof String;
  }

  @Override
  public String getIdent() {
    return "string";
  }

  @Override
  public String getNamespace() {
    return "";
  }
}

package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ValueRef implements IInputValue {
  public String valueIdent;
  public Type type;

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Object getValue() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}

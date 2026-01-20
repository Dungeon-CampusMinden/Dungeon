package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DynamicValue implements IValue {
  public String ident;
  public Type type;

  private static int idCounter = 0;

  @JsonIgnore
  public void setIdentUnique(String ident){
    this.ident = ident + "_" + idCounter++;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Object getValue() {
    throw new UnsupportedOperationException();
  }
}

package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.Type;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ValueRef implements IInputValue {
  private final String valueIdent;
  private final Type type;

  public ValueRef() {
    valueIdent = null;
    type = null;
  }

  public ValueRef(DynamicValue value) {
    this.valueIdent = value.getIdent();
    this.type = value.getType();
  }

  @JsonCreator
  public ValueRef(@JsonProperty("valueIdent") String valueIdent, @JsonProperty("type") Type type) {
    this.valueIdent = valueIdent;
    this.type = type;
  }

  public String getValueIdent() {
    return valueIdent;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Object getValue() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}

package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.Type;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConstantValue implements IInputValue {
  private final Type type;
  private final Object value;

  public ConstantValue() {
    type = null;
    value = null;
  }

  @JsonCreator
  public ConstantValue(@JsonProperty("type") Type type, @JsonProperty("value") Object value) {
    this.type = type;
    this.value = value;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  @JsonIgnore(value = false)
  public Object getValue() {
    return value;
  }
}

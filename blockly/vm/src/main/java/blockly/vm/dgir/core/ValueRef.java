package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.IType;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ValueRef implements IInputValue {
  private final String refValueLabel;
  private final IType type;


  public ValueRef(String refValueLabel, IType type) {
    this.refValueLabel = refValueLabel;
    this.type = type;
  }

  public ValueRef(DynamicValue value) {
    this(value.getLabel(), value.getType());
  }

  public String getRefValueLabel() {
    return refValueLabel;
  }

  @Override
  public IType getType() {
    return type;
  }

  @Override
  @JsonIgnore
  public Object getValue() {
    throw new UnsupportedOperationException();
  }
}

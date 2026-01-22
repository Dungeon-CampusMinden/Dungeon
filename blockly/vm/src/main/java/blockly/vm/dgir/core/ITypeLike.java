package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public sealed interface ITypeLike permits Type, Value, ValueOperand {
  public Type getType();
}

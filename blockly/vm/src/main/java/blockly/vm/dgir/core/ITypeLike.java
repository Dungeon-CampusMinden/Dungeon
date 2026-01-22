package blockly.vm.dgir.core;

public sealed interface ITypeLike permits Type, Value, ValueOperand {
  Type getType();
}

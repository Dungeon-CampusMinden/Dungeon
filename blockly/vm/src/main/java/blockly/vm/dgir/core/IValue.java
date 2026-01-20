package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.IType;

public interface IValue {
  String getLabel();

  IType getType();

  Object getValue();
}

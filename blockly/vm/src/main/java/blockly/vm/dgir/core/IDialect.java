package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.Type;

import java.util.List;

public interface IDialect {
  String getNamespace();
  List<Operation> AllOperations();
  List<Type> AllTypes();
}

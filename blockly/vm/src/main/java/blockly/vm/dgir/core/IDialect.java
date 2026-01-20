package blockly.vm.dgir.core;

import java.util.List;

public interface IDialect {
  String getNamespace();
  List<Operation> AllOperations();
}

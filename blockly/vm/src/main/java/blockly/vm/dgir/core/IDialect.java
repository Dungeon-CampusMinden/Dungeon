package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public interface IDialect {
  String getNamespace();
  List<Operation> AllOperations();
}

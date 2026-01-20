package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.IDialect;
import blockly.vm.dgir.core.Operation;

import java.util.List;

public class Func implements IDialect {
  @Override
  public String getNamespace() {
    return "func";
  }

  @Override
  public List<Operation> AllOperations() {
    return List.of();
  }
}

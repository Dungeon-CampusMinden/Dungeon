package blockly.vm.dgir.dialect.io;

import blockly.vm.dgir.core.IDialect;
import blockly.vm.dgir.core.Operation;

import java.util.List;

public class IO implements IDialect {
  @Override
  public String getNamespace() {
    return "io";
  }

  @Override
  public List<Operation> AllOperations() {
    return List.of(new PrintOp());
  }
}

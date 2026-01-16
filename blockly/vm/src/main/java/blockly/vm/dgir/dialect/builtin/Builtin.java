package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.DialectRegistry;
import blockly.vm.dgir.core.IDialect;

public class Builtin implements IDialect {
  @Override
  public String getNamespace() {
    return "";
  }

  @Override
  public void Register() {
    // Register operations here
    DialectRegistry.add(new ProgramOp());
  }
}


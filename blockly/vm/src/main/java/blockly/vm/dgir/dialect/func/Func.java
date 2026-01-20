package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.IDialect;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Type;

import java.util.List;

public class Func implements IDialect {
  @Override
  public String getNamespace() {
    return "func";
  }

  @Override
  public List<Operation> AllOperations() {
    return List.of(
      new FuncOp()
    );
  }

  @Override
  public List<Type> AllTypes() {
    return List.of();
  }
}

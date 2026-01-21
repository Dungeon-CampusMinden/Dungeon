package blockly.vm.dgir.dialect.arith;

import blockly.vm.dgir.core.IDialect;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.type.Type;

import java.util.List;

public class Arith implements IDialect {
  @Override
  public String getNamespace() {
    return "arith";
  }

  @Override
  public List<Operation> AllOperations() {
    return List.of(
      new ConstantOp()
    );
  }

  @Override
  public List<Type> AllTypes() {
    return List.of();
  }
}

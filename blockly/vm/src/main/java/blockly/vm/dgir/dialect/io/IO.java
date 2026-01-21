package blockly.vm.dgir.dialect.io;

import blockly.vm.dgir.core.Attribute;
import blockly.vm.dgir.core.IDialect;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.type.Type;

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

  @Override
  public List<Type> AllTypes() {
    return List.of();
  }

  @Override
  public List<Attribute> AllAttributes() {
    return List.of();
  }
}

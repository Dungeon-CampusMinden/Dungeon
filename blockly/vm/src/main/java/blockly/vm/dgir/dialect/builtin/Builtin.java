package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.IDialect;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.dialect.builtin.types.Float32_t;
import blockly.vm.dgir.dialect.builtin.types.Int32_t;
import blockly.vm.dgir.dialect.builtin.types.String_t;

import java.util.List;

public class Builtin implements IDialect {
  @Override
  public String getNamespace() {
    return "";
  }

  @Override
  public List<Operation> AllOperations() {
    return List.of(
      new ProgramOp()
    );
  }

  @Override
  public List<Type> AllTypes() {
    return List.of(
      Int32_t.INSTANCE,
      Float32_t.INSTANCE,
      String_t.INSTANCE
    );
  }
}


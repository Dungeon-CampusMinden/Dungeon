package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.func.types.FuncType;

import java.util.List;

public class Func extends Dialect {
  @Override
  public String getNamespace() {
    return "func";
  }

  @Override
  public List<Op> allOps() {
    return List.of(
      new FuncOp(),
      new ReturnOp()
    );
  }

  @Override
  public List<Type> allTypes() {
    return List.of(
      FuncType.INSTANCE
    );
  }

  @Override
  public List<Attribute> allAttributes() {
    return List.of();
  }
}

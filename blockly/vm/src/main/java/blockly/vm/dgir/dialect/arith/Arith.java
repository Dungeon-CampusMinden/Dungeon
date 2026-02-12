package blockly.vm.dgir.dialect.arith;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.ir.Attribute;
import blockly.vm.dgir.core.ir.Op;
import blockly.vm.dgir.core.ir.Type;

import java.util.List;

public class Arith extends Dialect {
  @Override
  public String getNamespace() {
    return "arith";
  }

  @Override
  public List<Op> allOps() {
    return List.of(
      new ConstantOp()
    );
  }

  @Override
  public List<Type> allTypes() {
    return List.of();
  }

  @Override
  public List<Attribute> allAttributes() {
    return List.of();
  }
}

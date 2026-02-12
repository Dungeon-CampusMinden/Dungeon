package blockly.vm.dgir.dialect.cf;

import blockly.vm.dgir.core.Dialect;
import blockly.vm.dgir.core.ir.Attribute;
import blockly.vm.dgir.core.ir.Op;
import blockly.vm.dgir.core.ir.Type;

import java.util.List;

public class CF extends Dialect {
  @Override
  public String getNamespace() {
    return "cf";
  }

  @Override
  public List<Op> allOps() {
    return List.of(
      new BranchOp(),
      new BranchCondOp()
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

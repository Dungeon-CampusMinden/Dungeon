package dialect.scf;

import core.Dialect;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;

import java.util.List;

public class SCF extends Dialect {
  @Override
  public String getNamespace() {
    return "scf";
  }

  @Override
  public List<Op> allOps() {
    return List.of(
      new ContinueOp(),
      new ForOp(),
      new IfOp(),
      new ScopeOp()
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

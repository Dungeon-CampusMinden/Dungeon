package dialect.cf;

import core.Dialect;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;

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

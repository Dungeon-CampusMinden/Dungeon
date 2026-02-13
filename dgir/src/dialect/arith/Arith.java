package dialect.arith;

import core.*;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;

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

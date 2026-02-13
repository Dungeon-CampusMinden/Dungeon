package dialect.io;

import core.*;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;

import java.util.List;

public class IO extends Dialect {
  @Override
  public String getNamespace() {
    return "io";
  }

  @Override
  public List<Op> allOps() {
    return List.of(new PrintOp());
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

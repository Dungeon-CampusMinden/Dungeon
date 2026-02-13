package dialect.func;

import core.*;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import dialect.func.types.FuncType;

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
      new ReturnOp(),
      new CallOp()
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

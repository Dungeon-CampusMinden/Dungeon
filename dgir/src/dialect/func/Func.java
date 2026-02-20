package dialect.func;

import core.*;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import dialect.func.types.FuncType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class Func extends Dialect {
  @Override
  public @NotNull String getNamespace() {
    return "func";
  }

  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return List.of(
      new FuncOp(),
      new ReturnOp(),
      new CallOp()
    );
  }

  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return List.of(
      FuncType.INSTANCE
    );
  }

  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return List.of();
  }
}

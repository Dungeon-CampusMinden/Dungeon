package dialect.func;

import core.*;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import dialect.func.types.FuncType;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class Func extends Dialect {
  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "func";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return List.of(new FuncOp(), new ReturnOp(), new CallOp());
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return List.of(FuncType.INSTANCE);
  }

  @Contract(pure = true)
  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return List.of();
  }
}

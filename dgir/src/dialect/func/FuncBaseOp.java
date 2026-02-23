package dialect.func;

import core.Dialect;
import core.ir.Op;
import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class FuncBaseOp extends Op {
  public FuncBaseOp() {
    super();
  }

  public FuncBaseOp(Operation operation) {
    super(operation);
  }

  @Contract(pure = true)
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return FuncDialect.class;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "func";
  }
}


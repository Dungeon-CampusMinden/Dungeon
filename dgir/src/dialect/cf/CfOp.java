package dialect.cf;

import core.Dialect;
import core.ir.Op;
import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class CfOp extends Op {
  public CfOp() {
    super();
  }

  public CfOp(Operation operation) {
    super(operation);
  }

  @Contract(pure = true)
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return CfDialect.class;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "cf";
  }
}


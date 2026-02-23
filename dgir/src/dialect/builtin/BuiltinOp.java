package dialect.builtin;

import core.Dialect;
import core.ir.Op;
import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class BuiltinOp extends Op {
  public BuiltinOp() {
    super();
  }

  public BuiltinOp(Operation operation) {
    super(operation);
  }

  @Contract(pure = true)
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return BuiltinDialect.class;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "";
  }
}


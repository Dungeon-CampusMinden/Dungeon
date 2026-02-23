package dialect.io;

import core.Dialect;
import core.ir.Op;
import core.ir.Operation;
import org.jetbrains.annotations.NotNull;

public abstract class IoOp extends Op {
  public IoOp() {
    super();
  }

  public IoOp(Operation operation) {
    super(operation);
  }

  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return IoDialect.class;
  }

  @Override
  public @NotNull String getNamespace() {
    return "io";
  }
}

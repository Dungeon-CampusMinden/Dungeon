package dialect.scf;

import core.Dialect;
import core.ir.Op;
import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public abstract class ScfOp extends Op {
  @Contract(pure = true)
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return SCFDialect.class;
  }

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "scf";
  }

  public ScfOp() {
    super();
  }

  public ScfOp(Operation operation) {
    super(operation);
  }
}

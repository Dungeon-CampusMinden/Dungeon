package dialect.arith.attributes;

import core.Dialect;
import core.ir.Attribute;
import dialect.arith.ArithDialect;
import org.jetbrains.annotations.NotNull;

public abstract class ArithAttribute extends Attribute {
  @Override
  public @NotNull String getNamespace() {
    return "arith";
  }

  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return ArithDialect.class;
  }

  protected ArithAttribute() {
    super();
  }
}

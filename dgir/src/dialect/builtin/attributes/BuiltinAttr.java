package dialect.builtin.attributes;

import core.Dialect;
import core.ir.Attribute;
import dialect.builtin.BuiltinDialect;
import org.jetbrains.annotations.NotNull;

public abstract class BuiltinAttr extends Attribute {
  @Override
  public @NotNull String getNamespace() {
    return "";
  }

  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return BuiltinDialect.class;
  }
}

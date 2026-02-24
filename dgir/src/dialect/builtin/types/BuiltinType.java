package dialect.builtin.types;

import core.Dialect;
import core.ir.Type;
import dialect.builtin.BuiltinDialect;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all types contributed by the {@link BuiltinDialect}.
 *
 * <p>Subclasses must implement {@link #getIdent()}, {@link #getValidator()}, and, for parameterized
 * types, {@link #getParameterizedIdent()} and {@link #getParameterizedStringFactory()}.
 */
public abstract class BuiltinType extends Type {

  @Override
  public @NotNull String getNamespace() {
    return "";
  }

  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return BuiltinDialect.class;
  }
}

package dialect.func.types;

import core.Dialect;
import core.ir.Type;
import dialect.func.FuncDialect;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for all types contributed by the {@link FuncDialect}.
 *
 * <p>Subclasses must implement {@link #getIdent()}, {@link #getValidator()}, and, for parameterized
 * types, {@link #getParameterizedIdent()} and {@link #getParameterizedStringFactory()}.
 */
public abstract class FuncBaseType extends Type {

  @Override
  public @NotNull String getNamespace() {
    return "func";
  }

  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return FuncDialect.class;
  }
}

package dialect.builtin.attributes;

import core.Dialect;
import core.ir.Type;
import core.ir.TypedAttribute;
import dialect.builtin.BuiltinDialect;
import org.jetbrains.annotations.NotNull;

public abstract class BuiltinTypedAttr extends TypedAttribute {
  /**
   * Create a typed attribute associated with the given type.
   *
   * @param type the type that governs validation of the stored value.
   */
  protected BuiltinTypedAttr(@NotNull Type type) {
    super(type);
  }

  @Override
  public @NotNull String getNamespace() {
    return "";
  }

  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return BuiltinDialect.class;
  }
}

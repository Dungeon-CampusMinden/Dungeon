package dialect.func;

import core.Dialect;
import core.Utils;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import dialect.func.types.FuncType;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * The {@code func} dialect provides function-definition and call operations.
 *
 * <p>Namespace: {@code func}
 *
 * <p>Operations: {@link Func} (sealed interface enumerating all ops)
 *
 * <ul>
 *   <li>{@link FuncOp} — declares a named function with a body region
 *   <li>{@link CallOp} — calls a named function
 *   <li>{@link ReturnOp} — returns from a function, optionally with a value
 * </ul>
 *
 * <p>Types:
 *
 * <ul>
 *   <li>{@link FuncType} — a function signature ({@code (inputs) -> output})
 * </ul>
 */
public class FuncDialect extends Dialect {

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "func";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return Utils.Dialect.allOps(FuncDialect.class, Func.class);
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return List.of(FuncType.INSTANCE);
  }

  @Contract(pure = true)
  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return List.of();
  }
}



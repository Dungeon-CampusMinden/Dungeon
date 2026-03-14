package dgir.dialect.func;

import dgir.core.Dialect;
import dgir.core.DgirCoreUtils;
import dgir.core.ir.Attribute;
import dgir.core.ir.Op;
import dgir.core.ir.Type;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static dgir.dialect.func.FuncOps.*;
import static dgir.dialect.func.FuncTypes.*;

/**
 * The {@code func} dialect provides function-definition and call operations.
 *
 * <p>Namespace: {@code func}
 *
 * <p>Operations: {@link FuncOps} (sealed interface enumerating all ops)
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
    return DgirCoreUtils.Dialect.allOps(FuncDialect.class, FuncOps.class);
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return DgirCoreUtils.Dialect.allTypes(FuncDialect.class, FuncTypes.class);
  }

  @Contract(pure = true)
  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return List.of();
  }
}

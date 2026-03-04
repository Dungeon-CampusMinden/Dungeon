package dgir.dialect.scf;

import dgir.core.Dialect;
import dgir.core.Utils;
import dgir.core.ir.Attribute;
import dgir.core.ir.Op;
import dgir.core.ir.Type;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static dgir.dialect.scf.ScfOps.*;

/**
 * The {@code scf} (structured control flow) dialect provides higher-level loop and conditional
 * constructs that map cleanly onto structured source-language control flow.
 *
 * <p>Namespace: {@code scf}
 *
 * <p>Operations: {@link ScfOps} (sealed interface enumerating all ops)
 *
 * <ul>
 *   <li>{@link ForOp} — counted for-loop with an induction variable
 *   <li>{@link IfOp} — conditional with an optional else branch
 *   <li>{@link ScopeOp} — opens a new variable scope with no other semantic effect
 *   <li>{@link BreakOp} — breaks out of the nearest enclosing {@link ForOp}
 *   <li>{@link ContinueOp} — marks the end of a structured control-flow region
 * </ul>
 */
public class ScfDialect extends Dialect {
  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "scf";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return Utils.Dialect.allOps(ScfDialect.class, ScfOps.class);
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return List.of();
  }

  @Contract(pure = true)
  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return List.of();
  }
}

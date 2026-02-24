package dialect.cf;

import core.Dialect;
import core.Utils;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * The {@code cf} dialect provides low-level control-flow operations.
 *
 * <p>Namespace: {@code cf}
 *
 * <p>Operations: {@link CF} (sealed interface enumerating all ops)
 *
 * <ul>
 *   <li>{@link BranchOp} — unconditional branch to a target block
 *   <li>{@link BranchCondOp} — conditional branch choosing between two target blocks
 * </ul>
 */
public class CfDialect extends Dialect {

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "cf";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return Utils.Dialect.allOps(CfDialect.class, CF.class);
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



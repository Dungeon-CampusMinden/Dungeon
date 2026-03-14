package dgir.dialect.cf;

import dgir.core.Dialect;
import dgir.core.DgirCoreUtils;
import dgir.core.ir.Attribute;
import dgir.core.ir.Op;
import dgir.core.ir.Type;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static dgir.dialect.cf.CfOps.*;

/**
 * The {@code cf} dialect provides low-level control-flow operations.
 *
 * <p>Namespace: {@code cf}
 *
 * <p>Operations: {@link CfOps} (sealed interface enumerating all ops)
 *
 * <ul>
 *   <li>{@link BranchOp} — unconditional branch to a target block
 *   <li>{@link BranchCondOp} — conditional branch choosing between two target blocks
 * </ul>
 */
public class CfDialect extends Dialect {
  private static CfDialect instance;

  public static @NotNull CfDialect get() {
    synchronized (CfDialect.class) {
      if (instance == null) {
        instance = new CfDialect();
      }
    }
    return instance;
  }

  private CfDialect() {}

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "cf";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return DgirCoreUtils.Dialect.allOps(CfDialect.class, CfOps.class);
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

package dgir.dialect.arith;

import static dgir.dialect.arith.ArithOps.*;

import dgir.core.Dialect;
import dgir.core.DgirCoreUtils;
import dgir.core.ir.Attribute;
import dgir.core.ir.Op;
import dgir.core.ir.Type;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * The {@code arith} dialect provides basic arithmetic operations.
 *
 * <p>Namespace: {@code arith}
 *
 * <p>Operations: {@link ArithOps} (sealed interface enumerating all ops)
 *
 * <ul>
 *   <li>{@link ConstantOp} — produces a constant value
 *   <li>{@link BinaryOp} — unified binary numeric operation
 *   <li>{@link BinaryOp} — compares two numeric operands
 *   <li>{@link CastOp} — casts a numeric operand to a target type
 * </ul>
 */
public class ArithDialect extends Dialect {

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "arith";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return DgirCoreUtils.Dialect.allOps(ArithDialect.class, ArithOps.class);
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return List.of();
  }

  @Contract(pure = true)
  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return DgirCoreUtils.Dialect.allAttributes(ArithDialect.class, ArithAttrs.class);
  }
}

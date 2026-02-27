package dialect.arith;

import core.Dialect;
import core.Utils;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import dialect.arith.attributes.CompModeAttr;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * The {@code arith} dialect provides basic arithmetic operations.
 *
 * <p>Namespace: {@code arith}
 *
 * <p>Operations: {@link Arith} (sealed interface enumerating all ops)
 *
 * <ul>
 *   <li>{@link ConstantOp} — produces a constant value
 *   <li>{@link AddOp} — adds two numeric operands
 *   <li>{@link SubOp} — subtracts two numeric operands
 *   <li>{@link MulOp} — multiplies two numeric operands
 *   <li>{@link DivOp} — divides two numeric operands
 *   <li>{@link RemOp} — remainder of two numeric operands
 *   <li>{@link CompareOp} — compares two numeric operands
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
    return Utils.Dialect.allOps(ArithDialect.class, Arith.class);
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return List.of();
  }

  @Contract(pure = true)
  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return List.of(new CompModeAttr());
  }
}

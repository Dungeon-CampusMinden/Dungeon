package dialect.builtin;

import core.Dialect;
import core.Utils;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import dialect.builtin.attributes.IntegerAttribute;
import dialect.builtin.attributes.StringAttribute;
import dialect.builtin.attributes.SymbolRefAttribute;
import dialect.builtin.attributes.TypeAttribute;
import dialect.builtin.types.FloatT;
import dialect.builtin.types.IntegerT;
import dialect.builtin.types.StringT;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * The {@code builtin} dialect provides the fundamental building blocks shared by all other
 * dialects.
 *
 * <p>Namespace: {@code ""} (empty — builtin idents have no prefix)
 *
 * <p>Operations: {@link Builtin} (sealed interface enumerating all ops)
 *
 * <ul>
 *   <li>{@link ProgramOp} — top-level container that must contain exactly one {@code main}
 *       function
 * </ul>
 *
 * <p>Types:
 *
 * <ul>
 *   <li>{@link IntegerT} — fixed-width integer ({@code int1/8/16/32/64})
 *   <li>{@link FloatT} — floating-point ({@code float32/64})
 *   <li>{@link StringT} — UTF-16 string ({@code string})
 * </ul>
 *
 * <p>Attributes:
 *
 * <ul>
 *   <li>{@link IntegerAttribute} — carries an integer value together with its {@link IntegerT} type
 *   <li>{@link StringAttribute} — carries a {@link String} value
 *   <li>{@link TypeAttribute} — wraps a {@link core.ir.Type} as an attribute
 *   <li>{@link SymbolRefAttribute} — references a symbol by name
 * </ul>
 */
public class BuiltinDialect extends Dialect {

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return Utils.Dialect.allOps(BuiltinDialect.class, Builtin.class);
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return List.of(
        IntegerT.INT1,
        IntegerT.INT8,
        IntegerT.INT16,
        IntegerT.INT32,
        IntegerT.INT64,
        FloatT.FLOAT32,
        FloatT.FLOAT64,
        StringT.INSTANCE);
  }

  @Contract(pure = true)
  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return List.of(
        IntegerAttribute.INSTANCE,
        StringAttribute.INSTANCE,
        TypeAttribute.INSTANCE,
        SymbolRefAttribute.INSTANCE);
  }
}



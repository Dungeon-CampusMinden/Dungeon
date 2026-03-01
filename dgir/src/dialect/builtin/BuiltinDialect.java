package dialect.builtin;

import core.Dialect;
import core.Utils;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static dialect.builtin.BuiltinOps.*;
import static dialect.builtin.BuiltinAttrs.*;
import static dialect.builtin.BuiltinTypes.*;

/**
 * The {@code builtin} dialect provides the fundamental building blocks shared by all other
 * dialects.
 *
 * <p>Namespace: {@code ""} (empty — builtin idents have no prefix)
 *
 * <p>Operations: {@link BuiltinOps} (sealed interface enumerating all ops)
 *
 * <ul>
 *   <li>{@link ProgramOp} — top-level container that must contain exactly one {@code main} function
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
    return Utils.Dialect.allOps(BuiltinDialect.class, BuiltinOps.class);
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return Utils.Dialect.allTypes(BuiltinDialect.class, BuiltinTypes.class);
  }

  @Contract(pure = true)
  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return Utils.Dialect.allAttributes(BuiltinDialect.class, BuiltinAttrs.class);
  }
}

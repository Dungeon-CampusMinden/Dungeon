package dialect.builtin;

import core.*;
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

public class Builtin extends Dialect {
  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return List.of(new ProgramOp());
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

package dialect.io;

import core.*;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class IO extends Dialect {
  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "io";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return List.of(new PrintOp());
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

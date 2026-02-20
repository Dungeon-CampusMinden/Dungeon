package dialect.io;

import core.*;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class IO extends Dialect {
  @Override
  public @NotNull String getNamespace() {
    return "io";
  }

  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return List.of(new PrintOp());
  }

  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return List.of();
  }

  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return List.of();
  }
}

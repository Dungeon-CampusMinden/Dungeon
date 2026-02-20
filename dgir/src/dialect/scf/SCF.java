package dialect.scf;

import core.Dialect;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class SCF extends Dialect {
  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "scf";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return List.of(
      new ContinueOp(),
      new ForOp(),
      new IfOp(),
      new ScopeOp()
    );
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

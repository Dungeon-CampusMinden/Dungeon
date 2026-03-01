package dialect.dg;

import core.Dialect;
import core.Utils;
import core.ir.Attribute;
import core.ir.Op;
import core.ir.Type;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public class DungeonDialect extends Dialect {
  @NotNull
  @Override
  public String getNamespace() {
    return "dg";
  }

  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return Utils.Dialect.allOps(DungeonDialect.class, DgOps.class);
  }

  @NotNull
  @Unmodifiable
  @Override
  public List<Type> allTypes() {
    return List.of();
  }

  @NotNull
  @Unmodifiable
  @Override
  public List<Attribute> allAttributes() {
    return Utils.Dialect.allAttributes(DungeonDialect.class, DgAttrs.class);
  }
}

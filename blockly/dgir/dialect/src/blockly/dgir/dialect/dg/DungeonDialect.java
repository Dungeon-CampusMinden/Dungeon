package blockly.dgir.dialect.dg;

import dgir.core.Dialect;
import dgir.core.Utils;
import dgir.core.ir.Attribute;
import dgir.core.ir.Op;
import dgir.core.ir.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

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

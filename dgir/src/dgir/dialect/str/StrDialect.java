package dgir.dialect.str;

import dgir.core.Dialect;
import dgir.core.DgirCoreUtils;
import dgir.core.ir.Attribute;
import dgir.core.ir.Op;
import dgir.core.ir.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public class StrDialect extends Dialect {
  @Override
  public @NotNull String getNamespace() {
    return "str";
  }

  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return DgirCoreUtils.Dialect.allOps(StrDialect.class, StrOps.class);
  }

  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return DgirCoreUtils.Dialect.allTypes(StrDialect.class, StrTypes.class);
  }

  @Override
  public @NotNull @Unmodifiable List<Attribute> allAttributes() {
    return DgirCoreUtils.Dialect.allAttributes(StrDialect.class, StrAttrs.class);
  }
}

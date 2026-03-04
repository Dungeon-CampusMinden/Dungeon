package dgir.vm.dialect.arith;

import core.Dialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import dialect.arith.ArithDialect;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ArithDialectRunner extends DialectRunner {
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return ArithDialect.class;
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(ArithDialectRunner.class, ArithRunners.class);
  }
}

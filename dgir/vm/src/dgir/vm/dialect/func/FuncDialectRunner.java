package dgir.vm.dialect.func;

import dgir.core.Dialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import dgir.dialect.func.FuncDialect;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class FuncDialectRunner extends DialectRunner {
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return FuncDialect.class;
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(FuncDialectRunner.class, FuncRunners.class);
  }
}

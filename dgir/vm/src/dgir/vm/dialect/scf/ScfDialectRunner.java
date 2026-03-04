package dgir.vm.dialect.scf;

import core.Dialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import dialect.scf.ScfDialect;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ScfDialectRunner extends DialectRunner {
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return ScfDialect.class;
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(ScfDialectRunner.class, ScfRunners.class);
  }
}

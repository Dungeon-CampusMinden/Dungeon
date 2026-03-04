package dgir.vm.dialect.cf;

import core.Dialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import dialect.cf.CfDialect;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class CfDialectRunner extends DialectRunner {
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return CfDialect.class;
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(CfDialectRunner.class, CfRunners.class);
  }
}

package dgir.vm.dialect.cf;

import dgir.core.Dialect;
import dgir.dialect.cf.CfDialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CfDialectRunner extends DialectRunner {
  private static CfDialectRunner instance;

  public static CfDialectRunner get() {
    synchronized (CfDialectRunner.class) {
      if (instance == null) {
        instance = new CfDialectRunner();
      }
    }
    return instance;
  }

  private CfDialectRunner() {}

  @Override
  public @NotNull Dialect getDialect() {
    return CfDialect.get();
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(CfDialectRunner.class, CfRunners.class);
  }
}

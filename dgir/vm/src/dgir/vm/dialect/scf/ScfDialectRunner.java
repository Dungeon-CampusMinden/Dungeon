package dgir.vm.dialect.scf;

import dgir.core.Dialect;
import dgir.dialect.scf.ScfDialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScfDialectRunner extends DialectRunner {
  private static ScfDialectRunner instance;

  public static ScfDialectRunner get() {
    synchronized (ScfDialectRunner.class) {
      if (instance == null) {
        instance = new ScfDialectRunner();
      }
    }
    return instance;
  }

  private ScfDialectRunner() {}

  @Override
  public @NotNull Dialect getDialect() {
    return ScfDialect.get();
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(ScfDialectRunner.class, ScfRunners.class);
  }
}

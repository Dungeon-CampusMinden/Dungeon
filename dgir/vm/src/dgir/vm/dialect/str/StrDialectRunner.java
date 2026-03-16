package dgir.vm.dialect.str;

import dgir.core.Dialect;
import dgir.dialect.str.StrDialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StrDialectRunner extends DialectRunner {
  private static StrDialectRunner instance;

  public static StrDialectRunner get() {
    synchronized (StrDialectRunner.class) {
      if (instance == null) {
        instance = new StrDialectRunner();
      }
    }
    return instance;
  }

  private StrDialectRunner() {}

  @Override
  public @NotNull Dialect getDialect() {
    return StrDialect.get();
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(StrDialectRunner.class, StrRunners.class);
  }
}

package dgir.vm.dialect.io;

import dgir.core.Dialect;
import dgir.dialect.io.IoDialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IoDialectRunner extends DialectRunner {
  private static IoDialectRunner instance;

  public static IoDialectRunner get() {
    synchronized (IoDialectRunner.class) {
      if (instance == null) {
        instance = new IoDialectRunner();
      }
    }
    return instance;
  }

  private IoDialectRunner() {}

  @Override
  public @NotNull Dialect getDialect() {
    return IoDialect.get();
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(IoDialectRunner.class, IoRunners.class);
  }
}

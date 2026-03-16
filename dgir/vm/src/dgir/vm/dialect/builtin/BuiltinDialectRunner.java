package dgir.vm.dialect.builtin;

import dgir.core.Dialect;
import dgir.dialect.builtin.BuiltinDialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BuiltinDialectRunner extends DialectRunner {
  private static BuiltinDialectRunner instance;

  public static BuiltinDialectRunner get() {
    synchronized (BuiltinDialectRunner.class) {
      if (instance == null) {
        instance = new BuiltinDialectRunner();
      }
    }
    return instance;
  }

  private BuiltinDialectRunner() {}

  @Override
  public @NotNull Dialect getDialect() {
    return BuiltinDialect.get();
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(BuiltinDialectRunner.class, BuiltinRunners.class);
  }
}

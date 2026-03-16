package dgir.vm.dialect.func;

import dgir.core.Dialect;
import dgir.dialect.func.FuncDialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FuncDialectRunner extends DialectRunner {
  private static FuncDialectRunner instance;

  public static FuncDialectRunner get() {
    synchronized (FuncDialectRunner.class) {
      if (instance == null) {
        instance = new FuncDialectRunner();
      }
    }
    return instance;
  }

  private FuncDialectRunner() {}

  @Override
  public @NotNull Dialect getDialect() {
    return FuncDialect.get();
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(FuncDialectRunner.class, FuncRunners.class);
  }
}

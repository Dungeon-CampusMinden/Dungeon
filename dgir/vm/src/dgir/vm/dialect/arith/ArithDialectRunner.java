package dgir.vm.dialect.arith;

import dgir.core.Dialect;
import dgir.dialect.arith.ArithDialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ArithDialectRunner extends DialectRunner {
  private static ArithDialectRunner instance;

  public static ArithDialectRunner get() {
    synchronized (ArithDialectRunner.class) {
      if (instance == null) {
        instance = new ArithDialectRunner();
      }
    }
    return instance;
  }

  private ArithDialectRunner() {}

  @Override
  public @NotNull Dialect getDialect() {
    return ArithDialect.get();
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(ArithDialectRunner.class, ArithRunners.class);
  }
}

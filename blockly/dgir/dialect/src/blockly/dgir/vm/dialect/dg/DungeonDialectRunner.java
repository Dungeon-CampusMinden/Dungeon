package blockly.dgir.vm.dialect.dg;

import blockly.dgir.dialect.dg.DungeonDialect;
import dgir.core.Dialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DungeonDialectRunner extends DialectRunner {
  private static DungeonDialectRunner instance;

  public static @NotNull DungeonDialectRunner get() {
    synchronized (DungeonDialectRunner.class) {
      if (instance == null) {
        instance = new DungeonDialectRunner();
      }
      return instance;
    }
  }

  private DungeonDialectRunner() {}

  @Override
  public @NotNull Dialect getDialect() {
    return DungeonDialect.get();
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(DungeonDialectRunner.class, DgRunners.class);
  }
}

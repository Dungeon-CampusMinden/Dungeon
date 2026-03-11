package blockly.dgir.vm.dialect.dg;

import blockly.dgir.dialect.dg.DungeonDialect;
import dgir.core.Dialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DungeonDialectRunner extends DialectRunner {
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return DungeonDialect.class;
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(DungeonDialectRunner.class, DgRunners.class);
  }
}

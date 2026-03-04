package dgir.vm.dialect.io;

import dgir.core.Dialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import dgir.dialect.io.IoDialect;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class IoDialectRunner extends DialectRunner {
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return IoDialect.class;
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(IoDialectRunner.class, IoRunners.class);
  }
}

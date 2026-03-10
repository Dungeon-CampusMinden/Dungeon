package dgir.vm.dialect.str;

import dgir.core.Dialect;
import dgir.dialect.str.StrDialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StrDialectRunner extends DialectRunner {
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return StrDialect.class;
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(StrDialectRunner.class, StrRunners.class);
  }
}

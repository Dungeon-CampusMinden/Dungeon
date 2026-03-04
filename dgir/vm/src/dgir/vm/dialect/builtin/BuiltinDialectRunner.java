package dgir.vm.dialect.builtin;

import dgir.core.Dialect;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunner;
import dgir.dialect.builtin.BuiltinDialect;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class BuiltinDialectRunner extends DialectRunner {
  @Override
  public @NotNull Class<? extends Dialect> getDialect() {
    return BuiltinDialect.class;
  }

  @Override
  public @NotNull List<@NotNull OpRunner> allRunners() {
    return allRunners(BuiltinDialectRunner.class, BuiltinRunners.class);
  }
}

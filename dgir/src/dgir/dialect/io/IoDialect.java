package dgir.dialect.io;

import dgir.core.Dialect;
import dgir.core.DgirCoreUtils;
import dgir.core.ir.Attribute;
import dgir.core.ir.Op;
import dgir.core.ir.Type;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static dgir.dialect.io.IoOps.*;

/**
 * The {@code io} dialect provides basic console input/output operations.
 *
 * <p>Namespace: {@code io}
 *
 * <p>Operations: {@link IoOps} (sealed interface enumerating all ops)
 *
 * <ul>
 *   <li>{@link PrintOp} — prints one or more values to standard output
 *   <li>{@link ConsoleInOp} — reads a line from standard input and returns it as a typed value
 * </ul>
 */
public class IoDialect extends Dialect {
  private static IoDialect instance;

  public static @NotNull IoDialect get() {
    synchronized (IoDialect.class) {
      if (instance == null) {
        instance = new IoDialect();
      }
    }
    return instance;
  }

  private IoDialect() {}

  @Contract(pure = true)
  @Override
  public @NotNull String getNamespace() {
    return "io";
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Op> allOps() {
    return DgirCoreUtils.Dialect.allOps(IoDialect.class, IoOps.class);
  }

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Type> allTypes() {
    return List.of();
  }

  @Contract(pure = true)
  @Override
  public @Unmodifiable @NotNull List<Attribute> allAttributes() {
    return List.of();
  }
}

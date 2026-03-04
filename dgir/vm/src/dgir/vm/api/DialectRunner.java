package dgir.vm.api;

import core.Dialect;
import dgir.vm.dialect.arith.ArithDialectRunner;
import dgir.vm.dialect.builtin.BuiltinDialectRunner;
import dgir.vm.dialect.cf.CfDialectRunner;
import dgir.vm.dialect.func.FuncDialectRunner;
import dgir.vm.dialect.io.IoDialectRunner;
import dgir.vm.dialect.scf.ScfDialectRunner;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

public abstract class DialectRunner {
  private static final @NotNull Map<Class<? extends DialectRunner>, List<@NotNull OpRunner>>
      dialectRunners = new HashMap<>();

  public abstract @NotNull Class<? extends Dialect> getDialect();

  public abstract @NotNull List<@NotNull OpRunner> allRunners();

  public static void registerAllDialects() {
    OpRunnerRegistry.registerDialectRunner(new ArithDialectRunner());
    OpRunnerRegistry.registerDialectRunner(new BuiltinDialectRunner());
    OpRunnerRegistry.registerDialectRunner(new CfDialectRunner());
    OpRunnerRegistry.registerDialectRunner(new FuncDialectRunner());
    OpRunnerRegistry.registerDialectRunner(new IoDialectRunner());
    OpRunnerRegistry.registerDialectRunner(new ScfDialectRunner());
  }

  @NotNull
  @Unmodifiable
  public static List<@NotNull OpRunner> allRunners(
      @NotNull Class<? extends DialectRunner> dialect, @NotNull Class<?> diRunners) {
    // Check that diRunners is a sealed interface
    assert diRunners.isSealed() : diRunners.getSimpleName() + " interface must be sealed";

    if (dialectRunners.containsKey(dialect)) {
      return dialectRunners.get(dialect);
    }

    // Go over all permitted subclasses of this interface and collect their prototypes. This
    // allows
    // us to avoid
    // having to manually list all operations in the dialect, and instead just have them register
    // themselves via implementing
    // their dialect specific subclass.
    List<OpRunner> ops = new ArrayList<>();

    Class<?>[] permittedSubclasses = diRunners.getPermittedSubclasses();
    for (Class<?> subclass : permittedSubclasses) {
      // Get the default constructor for this operation and invoke it to get the prototype, then
      // add
      // it to the list of ops for this dialect.
      try {
        Constructor<?> defaultConstructor = subclass.getDeclaredConstructor();
        boolean isAccessible = defaultConstructor.canAccess(null);
        if (!isAccessible) defaultConstructor.setAccessible(true);
        try {
          OpRunner newOpRunner = (OpRunner) defaultConstructor.newInstance();
          ops.add(newOpRunner);
        } catch (InstantiationException e) {
          throw new RuntimeException(
              "Executing default constructor failed for OpRunner: " + subclass.getName(), e);
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
          throw new RuntimeException(e);
        }
        if (!isAccessible) defaultConstructor.setAccessible(false);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(
            "OpRunner class must have a default constructor: " + subclass.getName(), e);
      }
    }
    dialectRunners.put(dialect, ops);
    return ops;
  }
}

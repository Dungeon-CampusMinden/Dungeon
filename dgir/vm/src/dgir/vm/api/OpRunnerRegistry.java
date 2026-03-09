package dgir.vm.api;

import dgir.core.Dialect;
import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import dgir.core.ir.OperationDetails;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class is responsible for managing the registry of operation runners in the Blockly VM. It
 * stores a reference from an operation detail instance to a function handling the execution of that
 * operation. When an operation is executed in the VM, the OpRunnerRegistry is used to look up the
 * appropriate function to execute based on the operation's details.
 *
 * <p>This allows for a flexible and extensible way to define how different operations are executed
 * in the VM, as new operations can be added to the registry without modifying the core execution
 * logic of the VM.
 */
public class OpRunnerRegistry {
  private static final @NotNull Map<@NotNull Class<? extends Dialect>, @NotNull DialectRunner>
      dialectRunners = new HashMap<>();
  private static final @NotNull IdentityHashMap<@NotNull OperationDetails, @NotNull OpRunner>
      opRunners = new IdentityHashMap<>();

  private OpRunnerRegistry() {}

  public static void clearRunnerStates() {
    for (OpRunner runner : opRunners.values()) {
      runner.clearsState();
    }
  }

  public static void registerDialectRunner(@NotNull DialectRunner runner) {
    if (dialectRunners.containsKey(runner.getDialect())) return;
    registerOpRunners(runner.allRunners());
    dialectRunners.put(runner.getDialect(), runner);
  }

  public static @NotNull Optional<DialectRunner> getDialectRunner(
      @NotNull Class<? extends Dialect> dialect) {
    return Optional.ofNullable(dialectRunners.get(dialect));
  }

  public static void registerOpRunners(@NotNull List<OpRunner> runners) {
    for (OpRunner runner : runners) {
      OpRunnerRegistry.registerOpRunner(runner.getTargetOp(), runner);
    }
  }

  public static void registerOpRunner(@NotNull Op op, @NotNull OpRunner runner) {
    OpRunnerRegistry.registerOpRunner(op.getDetails(), runner);
  }

  public static void registerOpRunner(@NotNull Operation op, @NotNull OpRunner runner) {
    OpRunnerRegistry.registerOpRunner(op.getDetails(), runner);
  }

  public static void registerOpRunner(@NotNull OperationDetails details, @NotNull OpRunner runner) {
    opRunners.put(details, runner);
  }

  public static @NotNull Optional<OpRunner> getOpRunner(@NotNull Op op) {
    return getOpRunner(op.getDetails());
  }

  public static @NotNull Optional<OpRunner> getOpRunner(@NotNull Operation op) {
    return getOpRunner(op.getDetails());
  }

  public static @NotNull Optional<OpRunner> getOpRunner(@NotNull OperationDetails details) {
    return Optional.ofNullable(opRunners.get(details));
  }

  public static boolean hasOpRunner(@NotNull Op op) {
    return hasOpRunner(op.getDetails());
  }

  public static boolean hasOpRunner(@NotNull Operation op) {
    return hasOpRunner(op.getDetails());
  }

  public static boolean hasOpRunner(@NotNull OperationDetails details) {
    return opRunners.containsKey(details);
  }
}

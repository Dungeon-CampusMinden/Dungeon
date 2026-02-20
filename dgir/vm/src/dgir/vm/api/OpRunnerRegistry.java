package dgir.vm.api;

import core.detail.OperationDetails;
import core.ir.Op;
import core.ir.Operation;
import dgir.vm.dialect.arith.ConstantRunner;
import dgir.vm.dialect.builtin.ProgramRunner;
import dgir.vm.dialect.cf.BranchCondRunner;
import dgir.vm.dialect.cf.BranchRunner;
import dgir.vm.dialect.func.CallRunner;
import dgir.vm.dialect.func.FuncRunner;
import dgir.vm.dialect.func.ReturnRunner;
import dgir.vm.dialect.io.PrintRunner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  private static final @NotNull Map<OperationDetails, OpRunner> registry = new HashMap<>();

  private OpRunnerRegistry() {}

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
    registry.put(details, runner);
  }

  public static @Nullable OpRunner getOpRunner(@NotNull Op op) {
    return getOpRunner(op.getDetails());
  }

  public static @Nullable OpRunner getOpRunner(@NotNull Operation op) {
    return getOpRunner(op.getDetails());
  }

  public static @Nullable OpRunner getOpRunner(@NotNull OperationDetails details) {
    return registry.get(details);
  }

  public static boolean hasOpRunner(@NotNull Op op) {
    return hasOpRunner(op.getDetails());
  }

  public static boolean hasOpRunner(@NotNull Operation op) {
    return hasOpRunner(op.getDetails());
  }

  public static boolean hasOpRunner(@NotNull OperationDetails details) {
    return registry.containsKey(details);
  }

  public static void registerAllRunners() {
    // arith
    List<OpRunner> arithRunners = List.of(new ConstantRunner());
    registerOpRunners(arithRunners);

    // builtin
    List<OpRunner> builtinRunners = List.of(new ProgramRunner());
    registerOpRunners(builtinRunners);

    // cf
    List<OpRunner> cfRunners = List.of(new BranchCondRunner(), new BranchRunner());
    registerOpRunners(cfRunners);

    // func
    List<OpRunner> funcRunners = List.of(new CallRunner(), new FuncRunner(), new ReturnRunner());
    registerOpRunners(funcRunners);

    // io
    List<OpRunner> ioRunners = List.of(new PrintRunner());
    registerOpRunners(ioRunners);
  }
}

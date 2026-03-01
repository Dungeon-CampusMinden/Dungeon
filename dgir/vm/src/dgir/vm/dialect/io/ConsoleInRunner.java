package dgir.vm.dialect.io;

import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;

import static dialect.builtin.BuiltinTypes.*;
import static dialect.io.IoOps.ConsoleInOp;

public class ConsoleInRunner extends OpRunner {
  private static @NotNull InputStream in = System.in;
  private static @NotNull Scanner scanner = new Scanner(in, StandardCharsets.UTF_8);

  static {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  scanner.close();
                }));
  }

  public static void setInputStream(@NotNull InputStream in) {
    ConsoleInRunner.in = in;
    scanner = new Scanner(in, StandardCharsets.UTF_8);
  }

  public ConsoleInRunner() {
    super(ConsoleInOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    ConsoleInOp consoleInOp = op.as(ConsoleInOp.class).orElseThrow();

    try {
      switch (consoleInOp.getResultType()) {
        case StringT s -> state.setValueForOutput(op, scanner.nextLine());
        case IntegerT i -> {
          switch (i.getWidth()) {
            case 1 -> state.setValueForOutput(op, (byte) (scanner.nextByte() == 0 ? 0 : 1));
            case 8 -> state.setValueForOutput(op, scanner.nextByte());
            case 16 -> state.setValueForOutput(op, scanner.nextShort());
            case 32 -> state.setValueForOutput(op, scanner.nextInt());
            case 64 -> state.setValueForOutput(op, scanner.nextLong());
            default ->
                throw new IllegalStateException(
                    "Unsupported integer width for console input: " + i.getWidth());
          }
          // Consume the newline character
          scanner.nextLine();
        }
        case FloatT f -> {
          switch (f.getWidth()) {
            case 32 -> state.setValueForOutput(op, scanner.nextFloat());
            case 64 -> state.setValueForOutput(op, scanner.nextDouble());
            default ->
                throw new IllegalStateException(
                    "Unsupported float width for console input: " + f.getWidth());
          }
          // Consume the newline character
          scanner.nextLine();
        }
        default -> throw new IllegalStateException("Unsupported type for console input: " + op);
      }
    } catch (Exception e) {
      return Action.Abort(Optional.of(e), "Error reading from console: " + e.getMessage());
    }
    return Action.Next();
  }
}

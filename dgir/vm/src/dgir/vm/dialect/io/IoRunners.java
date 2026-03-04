package dgir.vm.dialect.io;

import dgir.core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dgir.dialect.builtin.BuiltinTypes;
import dgir.dialect.io.IoOps;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;
import org.jetbrains.annotations.NotNull;

public sealed interface IoRunners {
  final class ConsoleInRunner extends OpRunner implements IoRunners {
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
      super(IoOps.ConsoleInOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      IoOps.ConsoleInOp consoleInOp = op.as(IoOps.ConsoleInOp.class).orElseThrow();

      try {
        switch (consoleInOp.getResultType()) {
          case BuiltinTypes.StringT s -> state.setValueForOutput(op, scanner.nextLine());
          case BuiltinTypes.IntegerT i -> {
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
          case BuiltinTypes.FloatT f -> {
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

  final class PrintRunner extends OpRunner implements IoRunners {
    public static boolean parallelSystemOut = false;
    public static @NotNull PrintStream out = System.out;

    public PrintRunner() {
      super(IoOps.PrintOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      IoOps.PrintOp printOp = op.as(IoOps.PrintOp.class).orElseThrow();
      assert !printOp.getOperands().isEmpty() : "Print operation must have at least one operand";

      if (printOp.getOperands().size() == 1) {
        Object value = state.getValue(printOp.getOperand(0).orElseThrow());
        out.print(value);
        if (parallelSystemOut && out != System.out) {
          System.out.print(value);
        }
      } else {
        Object formatString = state.getValue(printOp.getOperand(0).orElseThrow());
        assert formatString instanceof String : "Format string must be a string";
        Object[] args =
            printOp.getOperands().subList(1, printOp.getOperands().size()).stream()
                .map(state::getValue)
                .toArray();
        out.printf(formatString.toString(), args);
        if (parallelSystemOut && out != System.out) {
          System.out.printf(formatString.toString(), args);
        }
      }

      return Action.Next();
    }
  }
}

package dialect.io;

import core.Dialect;
import core.detail.OperationDetails;
import core.ir.*;

import java.util.List;

import dialect.builtin.types.FloatT;
import dialect.builtin.types.IntegerT;
import dialect.builtin.types.StringT;
import org.jetbrains.annotations.NotNull;

/**
 * A blocking console input operation. This operation will read a line of input from the console and
 * return it as the return type of the operation.
 *
 * <p>If the input cannot be converted, this operation is expected to throw an exception.
 */
public class ConsoleInOp extends Op {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull OperationDetails.Impl createDetails() {
    class ConsoleInOpModel extends OperationDetails.Impl {
      ConsoleInOpModel() {
        super(ConsoleInOp.getIdent(), ConsoleInOp.class, Dialect.getOrThrow(IO.class), List.of());
      }

      @Override
      public boolean verify(@NotNull Operation operation) {
        ConsoleInOp consoleInOp = operation.as(ConsoleInOp.class).orElseThrow();
        // Check that the operation has exactly one result and that the type is either integer,
        // float or string.
        if (operation.getOutput().isEmpty()) {
          operation.emitError("ConsoleInOp must have exactly one result");
          return false;
        }
        switch (consoleInOp.getResultType()) {
          case IntegerT ignored -> {
            return true;
          }
          case FloatT ignored -> {
            return true;
          }
          case StringT ignored -> {
            return true;
          }
          default -> {
            operation.emitError("ConsoleInOp result type must be either integer, float or string");
            return false;
          }
        }
      }

      @Override
      public void populateDefaultAttrs(@NotNull List<NamedAttribute> attributes) {}
    }
    return new ConsoleInOpModel();
  }

  public static String getIdent() {
    return "io.consoleIn";
  }

  public static String getNamespace() {
    return "io";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public ConsoleInOp() {}

  public ConsoleInOp(Operation operation) {
    super(operation);
  }

  public ConsoleInOp(@NotNull Type type) {
    super(Operation.Create(getIdent(), null, null, type));
  }

  public @NotNull Value getResult() {
    return getOutputValue()
        .orElseThrow(() -> new IllegalStateException("No output value set for ConsoleInOp."));
  }

  public @NotNull Type getResultType() {
    return getResult().getType();
  }
}

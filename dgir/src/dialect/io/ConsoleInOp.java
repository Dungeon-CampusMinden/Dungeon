package dialect.io;

import core.ir.Operation;
import core.ir.Location;
import core.ir.Type;
import core.ir.Value;
import dialect.builtin.types.FloatT;
import dialect.builtin.types.IntegerT;
import dialect.builtin.types.StringT;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Blocking console-input operation in the {@code io} dialect.
 *
 * <p>Reads a single line from standard input and returns it converted to the declared result type.
 * If the input cannot be parsed as the target type, the runtime is expected to throw an exception.
 *
 * <p>The result type must be one of {@link IntegerT}, {@link FloatT}, or {@link StringT}.
 *
 * <p>Ident: {@code io.consoleIn}
 *
 * <pre>{@code
 * %n = io.consoleIn : int32
 * %s = io.consoleIn : string
 * }</pre>
 */
public final class ConsoleInOp extends IoOp implements IO {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "io.consoleIn";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return operation -> {
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
    };
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  private ConsoleInOp() {}

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public ConsoleInOp(Operation operation) {
    super(operation);
  }

  /**
   * Create a console-input op that produces a value of the given type.
   *
   * @param location the source location of this operation.
   * @param type the result type; must be {@link IntegerT}, {@link FloatT}, or {@link StringT}.
   */
  public ConsoleInOp(@NotNull Location location, @NotNull Type type) {
    setOperation(Operation.Create(location, this, null, null, type));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the output value produced by this operation.
   *
   * @return the result value.
   * @throws IllegalStateException if no output value has been set.
   */
  public @NotNull Value getResult() {
    return getOutputValue()
        .orElseThrow(() -> new IllegalStateException("No output value set for ConsoleInOp."));
  }

  /**
   * Returns the type of the value produced by this operation.
   *
   * @return the result type.
   */
  public @NotNull Type getResultType() {
    return getResult().getType();
  }
}

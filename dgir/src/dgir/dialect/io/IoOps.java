package dgir.dialect.io;

import dgir.core.Dialect;
import dgir.core.DgirCoreUtils;
import dgir.core.debug.Location;
import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import dgir.core.ir.Type;
import dgir.core.ir.Value;
import dgir.dialect.builtin.BuiltinTypes;
import dgir.dialect.str.StrTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Sealed marker interface for all operations in the {@link IoDialect}.
 *
 * <p>Every concrete op must both extend {@link IoOp} and implement this interface so that {@link
 * DgirCoreUtils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface IoOps {
  /**
   * Abstract base class for all operations in the {@code io} dialect.
   *
   * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
   * implement {@link IoOps} to be enumerated by {@link IoDialect}.
   */
  abstract class IoOp extends Op {

    /** Default constructor used during dialect registration. */
    IoOp() {
      super();
    }

    @Contract(pure = true)
    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return IoDialect.class;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getNamespace() {
      return "io";
    }
  }

  /**
   * Blocking console-input operation in the {@code io} dialect.
   *
   * <p>Reads a single line from standard input and returns it converted to the declared result
   * type. If the input cannot be parsed as the target type, the runtime is expected to throw an
   * exception.
   *
   * <p>The result type must be one of {@link BuiltinTypes.IntegerT}, {@link BuiltinTypes.FloatT},
   * or {@link BuiltinTypes.StringT}.
   *
   * <p>Ident: {@code io.consoleIn}
   *
   * <pre>{@code
   * %n = io.consoleIn : int32
   * %s = io.consoleIn : string
   * }</pre>
   */
  public final class ConsoleInOp extends IoOp implements IoOps {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    public @NotNull String getIdent() {
      return "io.consoleIn";
    }

    @Override
    public @NotNull Function<Operation, Boolean> getVerifier() {
      return operation -> {
        ConsoleInOp consoleInOp = operation.as(ConsoleInOp.class).orElseThrow();
        // Check that the operation has exactly one result and that the type is either integer,
        // float or string.
        if (operation.getOutput().isEmpty()) {
          operation.emitError("ConsoleInOp must have exactly one result");
          return false;
        }
        switch (consoleInOp.getResultType()) {
          case BuiltinTypes.IntegerT ignored -> {
            return true;
          }
          case BuiltinTypes.FloatT ignored -> {
            return true;
          }
          case StrTypes.StringT ignored -> {
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
     * Create a console-input op that produces a value of the given type.
     *
     * @param location the source location of this operation.
     * @param type the result type; must be {@link BuiltinTypes.IntegerT}, {@link
     *     BuiltinTypes.FloatT}, or {@link BuiltinTypes.StringT}.
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

  /**
   * Prints one or more values to standard output in the {@code io} dialect.
   *
   * <p>The op accepts a variable-length list of value operands. At least one operand must be
   * present; the verifier enforces this constraint.
   *
   * <p>Ident: {@code io.print}
   *
   * <pre>{@code
   * io.print %value
   * io.print %a, %b, %c
   * }</pre>
   */
  public final class PrintOp extends IoOp implements IoOps {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    public @NotNull String getIdent() {
      return "io.print";
    }

    @Override
    public @NotNull Function<Operation, Boolean> getVerifier() {
      return operation -> {
        PrintOp printOp = operation.as(PrintOp.class).orElseThrow();

        // The print op needs to have at least one operand
        if (printOp.getOperands().isEmpty()) {
          operation.emitError("Print operation must have at least one operand");
          return false;
        }
        return true;
      };
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    private PrintOp() {}

    /**
     * Create a print op with the given operand list.
     *
     * @param location the source location of this operation.
     * @param operands the values to print; must contain at least one element.
     */
    public PrintOp(@NotNull Location location, List<Value> operands) {
      setOperation(Operation.Create(location, this, operands, null, null));
    }

    /**
     * Create a print op with a varargs operand list.
     *
     * @param location the source location of this operation.
     * @param operands the values to print; must contain at least one element.
     */
    public PrintOp(@NotNull Location location, Value... operands) {
      this(location, List.of(operands));
    }
  }
}

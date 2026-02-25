package dialect.io;

import core.ir.Operation;
import core.ir.Location;
import core.ir.Value;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Prints one or more values to standard output in the {@code io} dialect.
 *
 * <p>The op accepts a variable-length list of value operands. At least one operand must be present;
 * the verifier enforces this constraint.
 *
 * <p>Ident: {@code io.print}
 *
 * <pre>{@code
 * io.print %value
 * io.print %a, %b, %c
 * }</pre>
 */
public final class PrintOp extends IoOp implements IO {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "io.print";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
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
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public PrintOp(Operation operation) {
    super(operation);
  }

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

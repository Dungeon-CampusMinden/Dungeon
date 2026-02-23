package dialect.io;

import core.*;
import core.detail.OperationDetails;
import core.ir.NamedAttribute;
import core.ir.Operation;
import core.ir.Value;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

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

  public PrintOp(Operation operation) {
    super(operation);
  }

  public PrintOp(List<Value> operands) {
    setOperation(Operation.Create(this, operands, null, null));
  }

  public PrintOp(Value... operands) {
    this(List.of(operands));
  }
}

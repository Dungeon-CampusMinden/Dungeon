package dialect.io;

import core.*;
import core.detail.OperationDetails;
import core.ir.NamedAttribute;
import core.ir.Op;
import core.ir.Operation;
import core.ir.Value;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class PrintOp extends Op {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.@NotNull Impl createDetails() {
    class PrintOpModel extends OperationDetails.Impl {
      PrintOpModel() {
        super(PrintOp.getIdent(), PrintOp.class, Dialect.getOrThrow(IO.class), List.of());
      }

      @Override
      public boolean verify(@NotNull Operation operation) {
        PrintOp printOp = operation.as(PrintOp.class).orElseThrow();

        // The print op needs to have at least one operand
        if (printOp.getOperands().isEmpty()) {
          operation.emitError("Print operation must have at least one operand");
          return false;
        }
        return true;
      }

      @Override
      public void populateDefaultAttrs(@NotNull List<NamedAttribute> attributes) {}
    }
    return new PrintOpModel();
  }

  public static String getIdent() {
    return "io.print";
  }

  public static String getNamespace() {
    return "io";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public PrintOp() {}

  public PrintOp(Operation operation) {
    super(operation);
  }

  public PrintOp(List<Value> operands) {
    super(Operation.Create(getIdent(), operands, null, null));
  }

  public PrintOp(Value... operands) {
    this(List.of(operands));
  }
}

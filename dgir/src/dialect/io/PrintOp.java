package dialect.io;

import core.*;
import core.detail.OperationDetails;
import core.ir.NamedAttribute;
import core.ir.Op;
import core.ir.Operation;
import core.ir.Value;

import java.util.List;


public class PrintOp extends Op {
  @Override
  public OperationDetails.Impl createDetails() {
    class PrintOpModel extends OperationDetails.Impl {
      PrintOpModel(String name, Class<? extends Op> type, Dialect dialect, List<String> attributeNames) {
        super(name, type, dialect, attributeNames);
      }

      @Override
      public boolean verify(Operation operation) {
        PrintOp printOp = operation.as(PrintOp.class).orElseThrow();

        // The print op needs to have at least one operand
        if (printOp.getOperands().isEmpty())
        {
          operation.emitError("Print operation must have at least one operand");
          return false;
        }

        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {

      }
    }
    return new PrintOpModel(getIdent(), this.getClass(), Dialect.get(IO.class), List.of());
  }

  public PrintOp() {
  }

  public PrintOp(Operation operation) {
    super(operation);
  }

  public PrintOp(List<Value> operands) {
    super(Operation.Create(getIdent(), operands, null, null));
  }

  public PrintOp(Value... operands) {
    this(List.of(operands));
  }

  public static String getIdent() {
    return "io.print";
  }

  public static String getNamespace() {
    return "io";
  }
}

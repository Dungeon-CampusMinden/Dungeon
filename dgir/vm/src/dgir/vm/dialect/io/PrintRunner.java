package dgir.vm.dialect.io;

import core.detail.RegisteredOperationDetails;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.io.PrintOp;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;

public class PrintRunner extends OpRunner {
  public static @NotNull PrintStream out = System.out;

  public PrintRunner() {
    super(RegisteredOperationDetails.lookup(PrintOp.class).orElseThrow());
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    PrintOp printOp = op.as(PrintOp.class).orElseThrow();
    assert !printOp.getOperands().isEmpty() : "Print operation must have at least one operand";

    if (printOp.getOperands().size() == 1) {
      Object value = state.getValue(printOp.getOperand(0).orElseThrow());
      out.print(value);
    } else {
      Object formatString = state.getValue(printOp.getOperand(0).orElseThrow());
      assert formatString instanceof String : "Format string must be a string";
      Object[] args = printOp.getOperands().subList(1, printOp.getOperands().size()).stream().map(state::getValue).toArray();
      out.printf(formatString.toString(), args);
    }

    return Action.Next();
  }
}

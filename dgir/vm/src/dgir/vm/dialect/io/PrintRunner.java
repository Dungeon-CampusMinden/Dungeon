package dgir.vm.dialect.io;

import core.detail.RegisteredOperationDetails;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.io.PrintOp;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.Optional;

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
      Object value = state.getValue(printOp.getOperands().getFirst()).orElseThrow();
      out.print(value);
    } else {
      Object formatString = state.getValue(printOp.getOperands().getFirst().getValue()).orElseThrow();
      assert formatString instanceof String : "Format string must be a string";
      Object[] args = printOp.getOperands().subList(1, printOp.getOperands().size()).stream().map(operand -> state.getValue(operand.getValue())).toArray();
      out.printf(formatString.toString(), args);
    }

    return Action.Next();
  }
}

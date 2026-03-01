package dgir.vm.dialect.func;

import core.SymbolTable;
import core.ir.Operation;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import static dialect.func.FuncOps.CallOp;

public class CallRunner extends OpRunner {
  public CallRunner() {
    super(CallOp.class);
  }

  @Override
  protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
    CallOp callOp = op.as(CallOp.class).orElseThrow();
    Object[] args = callOp.getOperands().stream().map(state::getValue).toArray();

    return Action.Call(
        SymbolTable.lookupSymbolInNearestTable(op, callOp.getCallee())
            .orElseThrow(() -> new AssertionError("Callee " + callOp.getCallee() + "not found.")),
        args);
  }
}

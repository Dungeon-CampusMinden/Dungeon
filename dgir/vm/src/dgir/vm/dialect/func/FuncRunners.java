package dgir.vm.dialect.func;

import core.SymbolTable;
import core.ir.Operation;
import core.ir.Value;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import dialect.func.FuncOps;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public sealed interface FuncRunners {
  final class FuncRunner extends OpRunner implements FuncRunners {
    public FuncRunner() {
      super(FuncOps.FuncOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      return Action.Next();
    }
  }

  final class CallRunner extends OpRunner implements FuncRunners {
    public CallRunner() {
      super(FuncOps.CallOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      FuncOps.CallOp callOp = op.as(FuncOps.CallOp.class).orElseThrow();
      Object[] args = callOp.getOperands().stream().map(state::getValue).toArray();

      return Action.Call(
          SymbolTable.lookupSymbolInNearestTable(op, callOp.getCallee())
              .orElseThrow(() -> new AssertionError("Callee " + callOp.getCallee() + "not found.")),
          args);
    }
  }

  final class ReturnRunner extends OpRunner implements FuncRunners {
    public ReturnRunner() {
      super(FuncOps.ReturnOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      FuncOps.ReturnOp returnOp = op.as(FuncOps.ReturnOp.class).orElseThrow();
      Optional<Value> returnValue = returnOp.getReturnValue();
      return returnValue
          .map(value -> Action.Terminate(state.getValue(value)))
          .orElseGet(() -> Action.Terminate(null));
    }
  }
}

package dgir.vm.dialect.func;

import dgir.core.SymbolTable;
import dgir.core.ir.Operation;
import dgir.dialect.builtin.BuiltinAttrs;
import dgir.dialect.func.FuncOps;
import dgir.vm.api.Action;
import dgir.vm.api.OpRunner;
import dgir.vm.api.State;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public sealed interface FuncRunners {
  Map<String, Operation> calleeCache = new HashMap<>();

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
    public void clearsState() {
      calleeCache.clear();
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      Object[] args = new Object[op.getOperands().size()];
      for (int i = 0; i < op.getOperands().size(); i++) {
        args[i] = state.getValueOrThrow(op.getOperandOrThrow(i));
      }

      Operation calleeOp =
          calleeCache.computeIfAbsent(
              op.getAttributeAsOrThrow("callee", BuiltinAttrs.SymbolRefAttribute.class).getValue(),
              calleeName ->
                  SymbolTable.lookupSymbolInNearestTable(op, calleeName)
                      .orElseThrow(
                          () -> new AssertionError("Callee " + calleeName + " not found.")));

      return Action.Call(calleeOp, args);
    }
  }

  final class ReturnRunner extends OpRunner implements FuncRunners {
    public ReturnRunner() {
      super(FuncOps.ReturnOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      var returnValue = op.getOperandValue(0);
      return returnValue
          .map(value -> Action.Terminate(state.getValueOrThrow(value), true))
          .orElseGet(() -> Action.Terminate(null, true));
    }
  }

  final class ConstantRunner extends OpRunner implements FuncRunners {
    public ConstantRunner() {
      super(FuncOps.ConstantOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      Operation calleeOp =
          calleeCache.computeIfAbsent(
              op.getAttributeAsOrThrow("callee", BuiltinAttrs.SymbolRefAttribute.class).getValue(),
              calleeName ->
                  SymbolTable.lookupSymbolInNearestTable(op, calleeName)
                      .orElseThrow(
                          () -> new AssertionError("Callee " + calleeName + " not found.")));
      state.setValueForOutput(op, calleeOp);
      return Action.Next();
    }
  }

  final class CallIndirectRunner extends OpRunner implements FuncRunners {
    public CallIndirectRunner() {
      super(FuncOps.CallIndirectOp.class);
    }

    @Override
    protected @NotNull Action runImpl(@NotNull Operation op, @NotNull State state) {
      // Only get the operands after the callee operand
      Object[] args = new Object[op.getOperands().size() - 1];
      for (int i = 0; i < op.getOperands().size() - 1; i++) {
        args[i] = state.getValueOrThrow(op.getOperandOrThrow(i + 1));
      }

      var callee = state.getValueAsOrThrow(op.getOperandOrThrow(0), Operation.class);

      return Action.Call(callee, args);
    }
  }
}

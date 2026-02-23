package dialect.builtin;

import core.ir.Block;
import core.ir.Operation;
import core.traits.*;
import dialect.func.FuncOp;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public final class ProgramOp extends BuiltinOp
    implements Builtin, ISymbolTable, INoTerminator, IGlobalContainer, ISingleRegion, ISingleBlock {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "program";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return operation -> {
      // Make sure that there is a toplevel func op with symbol_name "main"
      boolean hasMainFunc = false;
      Block block = operation.getRegions().getFirst().getBlocks().getFirst();
      for (Operation op : block.getOperations()) {
        var funcOp = op.as(FuncOp.class);
        if (funcOp.isPresent()) {
          if (funcOp.get().getFuncName().equals("main")) {
            if (hasMainFunc) {
              operation.emitError("There must be exactly one function with name main");
              return false;
            }
            hasMainFunc = true;
          }
        }
      }
      if (!hasMainFunc) {
        operation.emitError("There must be exactly one function with name main");
        return false;
      }
      return true;
    };
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public ProgramOp() {
    executeIfRegistered(
        ProgramOp.class,
        () -> setOperation(true, Operation.Create(this, null, null, null, 1)));
  }

  public ProgramOp(Operation operation) {
    super(operation);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public @NotNull FuncOp getMainFunc() {
    Block block = getBlock();
    for (Operation op : block.getOperations()) {
      var funcOp = op.as(FuncOp.class);
      if (funcOp.isPresent() && funcOp.get().getFuncName().equals("main")) {
        return funcOp.get();
      }
    }
    throw new IllegalStateException(
        "Could not find main function. This should have been caught by verification.");
  }
}

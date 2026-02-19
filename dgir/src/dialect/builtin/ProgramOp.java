package dialect.builtin;

import core.*;
import core.detail.OperationDetails;
import core.ir.Block;
import core.ir.NamedAttribute;
import core.ir.Op;
import core.ir.Operation;
import core.traits.*;
import dialect.func.FuncOp;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProgramOp extends Op implements ISymbolTable, INoTerminator, IGlobalContainer, ISingleRegion, ISingleBlock {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.Impl createDetails() {
    class ProgramOpModel extends OperationDetails.Impl {
      ProgramOpModel(String name, Class<? extends Op> type, Dialect dialect, List<String> attributeNames) {
        super(name, type, dialect, attributeNames);
      }

      @Override
      public boolean verify(Operation operation) {
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
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {

      }
    }

    return new ProgramOpModel(getIdent(), this.getClass(), Dialect.get(Builtin.class), List.of());
  }

  public static String getIdent() {
    return "program";
  }

  public static String getNamespace() {
    return "";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public ProgramOp() {
    executeIfRegistered(ProgramOp.class, () ->
      setOperation(true, Operation.Create(getIdent(), null, null, null, 1))
    );
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
    throw new IllegalStateException("Could not find main function. This should have been caught by verification.");
  }
}

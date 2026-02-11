package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.traits.IGlobalContainer;
import blockly.vm.dgir.core.traits.INoTerminator;
import blockly.vm.dgir.core.traits.ISymbol;
import blockly.vm.dgir.core.traits.ISymbolTable;
import blockly.vm.dgir.dialect.func.FuncOp;

import java.util.List;

public class ProgramOp extends Op implements ISymbolTable, INoTerminator, IGlobalContainer {
  @Override
  public OperationDetails.Impl createDetails() {
    class ProgramOpModel extends OperationDetails.Impl {
      ProgramOpModel(String name, Class<? extends Op> type, Dialect dialect, List<String> attributeNames) {
        super(name, type, dialect, attributeNames);
      }

      @Override
      public boolean verify(Operation operation) {
        // Make sure that there is a single region containing a single block
        if (operation.getRegions().size() != 1) {
          operation.emitError("Operation must have exactly one region");
          return false;
        }
        if (operation.getRegions().getFirst().getBlocks().size() != 1) {
          operation.emitError("Operation region must have exactly one block");
          return false;
        }

        // Make sure that there is a toplevel func op with symbol_name "main"
        boolean hasMainFunc = false;
        Block block = operation.getRegions().getFirst().getBlocks().getFirst();
        for (Operation op : block.getOperations()) {
          var funcOp = op.as(FuncOp.class);
          if (funcOp != null)
          {
            if (funcOp.getFuncName().equals("main")) {
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

  public ProgramOp() {
  }

  public ProgramOp(Operation operation) {
    super(operation);
  }

  public ProgramOp(boolean withRegion) {
    if (withRegion) {
      setOperation(Operation.Create(getIdent(), null, null, null, 1));
    }
    getRegions().getFirst().getEntryBlock();
  }

  public static String getIdent() {
    return "program";
  }


  public static String getNamespace() {
    return "";
  }
}

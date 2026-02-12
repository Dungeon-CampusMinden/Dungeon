package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.core.detail.OperationDetails;
import blockly.vm.dgir.core.detail.RegisteredOperationDetails;
import blockly.vm.dgir.core.ir.Block;
import blockly.vm.dgir.core.ir.NamedAttribute;
import blockly.vm.dgir.core.ir.Op;
import blockly.vm.dgir.core.ir.Operation;
import blockly.vm.dgir.core.traits.*;
import blockly.vm.dgir.dialect.func.FuncOp;
import blockly.vm.dgir.dialect.func.ReturnOp;

import java.util.List;

public class ProgramOp extends Op implements ISymbolTable, INoTerminator, IGlobalContainer, ISingleRegion, ISingleBlock {
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
    var details = RegisteredOperationDetails.lookup(getIdent());
    if (details.isPresent()) {
      setOperation(Operation.Create(getIdent(), null, null, null, 1));
      getRegions().getFirst().getEntryBlock();
    }
  }

  public ProgramOp(Operation operation) {
    super(operation);
  }

  public static String getIdent() {
    return "program";
  }

  public static String getNamespace() {
    return "";
  }
}

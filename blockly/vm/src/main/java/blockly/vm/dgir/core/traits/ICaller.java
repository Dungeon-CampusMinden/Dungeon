package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.SymbolTable;
import blockly.vm.dgir.dialect.builtin.attributes.SymbolRefAttribute;

public interface ICaller extends IOpTrait {
  static Operation getTargetOperation(Operation op) {
    var symbolTableOp = op.getParentWithTrait(ISymbolTable.class);
    if (symbolTableOp == null) {
      return null;
    }
    return symbolTableOp.lookupSymbol(
      op.getAttribute(SymbolRefAttribute.class, SymbolTable.getSymbolAttributeName())
        .getValue()
    );
  }

  default boolean verifyTrait(IOpTrait op) {
    return true;
  }
}

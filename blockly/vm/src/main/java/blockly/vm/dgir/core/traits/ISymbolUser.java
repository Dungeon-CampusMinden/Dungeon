package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.SymbolTable;
import blockly.vm.dgir.dialect.builtin.attributes.SymbolRefAttribute;

public interface ISymbolUser extends IOpTrait {
  default boolean verify(ISymbolUser trait) {
    var symbolName = trait.getSymbolRefAttribute().getValue();
    var symbolOp = SymbolTable.lookupSymbolInNearestTable(get(), symbolName);
    if (symbolOp.isEmpty()) {
      get().emitError("Could not find symbol " + symbolName);
      return false;
    }
    return true;
  }

  SymbolRefAttribute getSymbolRefAttribute();
}

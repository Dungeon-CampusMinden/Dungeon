package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.SymbolTable;

public interface ISymbolTable extends IOpTrait {
  default boolean verify(ISymbolTable trait) {
    if (get().getRegions().size() != 1){
      get().emitError("Symbol table must have exactly one region.");
      return false;
    }
    return true;
  }

  static Operation lookupSymbol(Operation op, String name) {
    return SymbolTable.lookupSymbolIn(op, name);
  }

  default Operation lookupSymbol(String name) {
    return lookupSymbol(get(), name);
  }
}

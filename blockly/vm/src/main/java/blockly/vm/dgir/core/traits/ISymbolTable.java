package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.SymbolTable;

public interface ISymbolTable extends IOpTrait {
  default Operation lookupSymbol(String name){
    return SymbolTable.lookupSymbolIn(get(), name);
  }
}

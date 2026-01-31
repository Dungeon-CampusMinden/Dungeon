package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.SymbolTable;

public interface ISymbolTable extends IOpTrait {
  static Operation lookupSymbol(Operation op, String name) {
    return SymbolTable.lookupSymbolIn(op, name);
  }

  static void verifyTrait(Operation op) {
    assert op.hasTrait(ISymbolTable.class) : "Operation does not implement ISymbolTable.";
    assert op.getRegions().size() == 1;
  }

  default Operation lookupSymbol(String name) {
    return lookupSymbol(get(), name);
  }
}

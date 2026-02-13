package core.traits;

import core.SymbolTable;
import dialect.builtin.attributes.SymbolRefAttribute;

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

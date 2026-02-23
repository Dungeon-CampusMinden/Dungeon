package core.traits;

import core.SymbolTable;
import dialect.builtin.attributes.SymbolRefAttribute;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ISymbolUser extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull ISymbolUser trait) {
    var symbolName = trait.getSymbolRefAttribute().getValue();
    var symbolOp = SymbolTable.lookupSymbolInNearestTable(getOperation(), symbolName);
    if (symbolOp.isEmpty()) {
      getOperation().emitError("Could not find symbol " + symbolName);
      return false;
    }
    return true;
  }

  @Contract(pure = true)
  @NotNull
  SymbolRefAttribute getSymbolRefAttribute();
}

package core.traits;

import core.ir.Operation;
import core.SymbolTable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ISymbolTable extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull ISymbolTable trait) {
    if (get().getRegions().size() != 1){
      get().emitError("Symbol table must have exactly one region.");
      return false;
    }
    return true;
  }

  @Contract(pure = true)
  static Operation lookupSymbol(Operation op, String name) {
    return SymbolTable.lookupSymbolIn(op, name);
  }

  @Contract(pure = true)
  default Operation lookupSymbol(String name) {
    return lookupSymbol(get(), name);
  }
}

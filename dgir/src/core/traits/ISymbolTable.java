package core.traits;

import core.SymbolTable;
import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ISymbolTable extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull ISymbolTable ignored) {
    if (get().getRegions().size() != 1) {
      get().emitError("Symbol table must have exactly one region.");
      return false;
    }
    return true;
  }

  @Contract(pure = true)
  static @Nullable Operation lookupSymbol(Operation op, String name) {
    return SymbolTable.lookupSymbolIn(op, name);
  }

  @Contract(pure = true)
  default @Nullable Operation lookupSymbol(String name) {
    return lookupSymbol(get(), name);
  }
}

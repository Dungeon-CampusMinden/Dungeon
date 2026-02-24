package core.traits;

import core.SymbolTable;
import dialect.builtin.attributes.SymbolRefAttribute;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Marks an operation that references a symbol by name and must be verifiable against that symbol.
 *
 * <p>The implementing op must provide {@link #getSymbolRefAttribute()}, which returns the
 * {@link SymbolRefAttribute} carrying the referenced symbol name. The verifier resolves the name in
 * the nearest enclosing {@link ISymbolTable} and emits an error if no matching symbol is found.
 *
 * <p>Examples: {@link dialect.func.CallOp}.
 */
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

package core.traits;

import core.SymbolTable;
import dialect.builtin.attributes.StringAttribute;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface ISymbol extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull ISymbol ignored) {
    if (!getOperation().getAttributes().containsKey(SymbolTable.getSymbolAttributeName())) {
      getOperation().emitError("Symbol must have a symbol attribute.");
      return false;
    }
    return true;
  }

  @Contract(pure = true)
  default @NotNull String getSymbol() {
    return getOperation()
        .getAttribute(StringAttribute.class, SymbolTable.getSymbolAttributeName())
        .orElseThrow()
        .getValue();
  }
}

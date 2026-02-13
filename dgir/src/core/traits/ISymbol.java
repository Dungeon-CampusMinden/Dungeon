package core.traits;

import core.SymbolTable;
import dialect.builtin.attributes.StringAttribute;

public interface ISymbol extends IOpTrait {
  default boolean verify(ISymbol trait) {
    if (!get().getAttributes().containsKey(SymbolTable.getSymbolAttributeName())){
      get().emitError("Symbol must have a symbol attribute.");
      return false;
    }
    return true;
  }

  default String getSymbol() {
    return get().getAttribute(StringAttribute.class, SymbolTable.getSymbolAttributeName()).getValue();
  }
}

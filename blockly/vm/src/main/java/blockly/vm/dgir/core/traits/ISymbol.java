package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.SymbolTable;
import blockly.vm.dgir.dialect.builtin.attributes.StringAttribute;

public interface ISymbol extends IOpTrait {
  default String getSymbol() {
    return get().getAttribute(StringAttribute.class, SymbolTable.getSymbolAttributeName()).getValue();
  }

  default boolean verifyTrait(ISymbol op) {
    return get().getAttributes().containsKey(SymbolTable.getSymbolAttributeName());
  }
}

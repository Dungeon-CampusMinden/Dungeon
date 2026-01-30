package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.SymbolTable;
import blockly.vm.dgir.dialect.builtin.attributes.StringAttribute;

public interface ISymbol extends IOpTrait {
  default String getSymbol() {
    return get().getAttribute(StringAttribute.class, SymbolTable.getSymbolAttributeName()).getValue();
  }

  @Override
  default boolean verifyTrait(Operation op) {
    return get().getAttributes().containsKey(SymbolTable.getSymbolAttributeName());
  }
}

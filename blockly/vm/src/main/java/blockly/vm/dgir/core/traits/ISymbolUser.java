package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.dialect.builtin.attributes.SymbolRefAttribute;

public interface ISymbolUser extends IOpTrait {
  SymbolRefAttribute getSymbolRefAttribute();
  boolean verifySymbolUser();
}

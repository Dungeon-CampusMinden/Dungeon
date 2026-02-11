package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.dialect.builtin.attributes.SymbolRefAttribute;

public interface ISymbolUser extends IOpTrait {
  default boolean verify(ISymbolUser trait) {
    return trait.verifySymbolUser();
  }
  
  SymbolRefAttribute getSymbolRefAttribute();
  boolean verifySymbolUser();
}

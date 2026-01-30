package blockly.vm.dgir.core;

import blockly.vm.dgir.core.opinterfaces.ISymbolTable;

import java.util.HashMap;
import java.util.Map;

/**
 * SymbolTable for ops which implement the ISymbolTable interface.
 * This allows managing the symbols of that op.
 */
public class SymbolTable {
  public SymbolTable(Operation owner) {
    assert owner.hasInterface(ISymbolTable.class);
    this.owner = owner;
  }

  private final Operation owner;
  private final Map<String, Operation> symbols = new HashMap<>();
}

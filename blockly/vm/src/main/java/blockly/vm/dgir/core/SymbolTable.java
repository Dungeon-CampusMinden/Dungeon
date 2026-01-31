package blockly.vm.dgir.core;

import blockly.vm.dgir.core.traits.ISymbolTable;
import blockly.vm.dgir.dialect.builtin.attributes.StringAttribute;

import java.util.HashMap;
import java.util.Map;

/**
 * SymbolTable for ops which implement the ISymbolTable interface.
 * This allows managing the symbols of that op.
 */
public class SymbolTable {
  public SymbolTable(Operation owner) {
    assert owner.hasTrait(ISymbolTable.class);
    this.owner = owner;
  }

  private final Operation owner;
  private final Map<String, Operation> symbols = new HashMap<>();

  public static Operation lookupSymbolIn(Operation operation, String symbolName) {
    assert operation.hasTrait(ISymbolTable.class);
    Region region = operation.getFirstRegion();
    if (region.getBlocks().isEmpty())
      return null;

    for (Operation op : region.getBlocks().getFirst().getOperations()) {
      String name = getNameIfSymbol(op, getSymbolAttributeName());
      if (name != null && name.equals(symbolName)) {
        return op;
      }
    }
    return null;
  }

  private static String getNameIfSymbol(Operation op, String symbolAttributeName) {
    var attr = op.getAttribute(StringAttribute.class, symbolAttributeName);
    return attr == null ? null : attr.getValue();
  }

  public static String getSymbolAttributeName() {
    return "symbol_name";
  }
}

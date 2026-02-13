package core;

import core.ir.Block;
import core.ir.Op;
import core.ir.Operation;
import core.ir.Region;
import core.traits.ISymbol;
import core.traits.ISymbolTable;
import dialect.builtin.attributes.StringAttribute;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * SymbolTable for ops which implement the ISymbolTable interface.
 * This allows managing the symbols of that op.
 */
public class SymbolTable {
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

  public static Optional<Operation> nearestSymbolTable(Operation from) {
    assert from != null : "from operation cannot be null";

    if (from.hasTrait(ISymbolTable.class)) {
      return Optional.of(from);
    }

    var symbolTableOp = from.getParentWithTrait(ISymbolTable.class);
    if (symbolTableOp != null) {
      return Optional.of(symbolTableOp.get());
    }

    return Optional.empty();
  }

  public static Optional<Operation> lookupSymbolInNearestTable(Operation from, String symbolName) {
    Optional<Operation> symbolTableOp = nearestSymbolTable(from);
    if (symbolTableOp.isEmpty()) {
      return Optional.empty();
    }
    Operation foundOp = lookupSymbolIn(symbolTableOp.get(), symbolName);
    return Optional.ofNullable(foundOp);
  }

  public static <T extends Op & ISymbol> Optional<T> lookupSymbolInNearestTableAsOp(Operation from, String symbolName, Class<T> clazz) {
    Optional<Operation> foundOp = lookupSymbolInNearestTable(from, symbolName);
    if (foundOp.isEmpty() || !foundOp.get().isa(clazz)) {
      return Optional.empty();
    }
    return Optional.ofNullable(foundOp.get().as(clazz));
  }

  public static String getSymbolAttributeName() {
    return "symbol_name";
  }

  /* Walk all of the operations within the given set of regions, without
   * traversing into any nested symbol tables. Stops walking if the result of the
   * callback is anything other than `WalkResult::advance`.
   */
  private static Optional<WalkResult> walk(List<Region> regions, Function<Operation, Optional<WalkResult>> callback) {
    for (Region region : regions) {
      for (Block block : region.getBlocks()) {
        for (Operation operation : block.getOperations()) {
          if (operation.hasTrait(ISymbolTable.class)) {
            continue;
          }
          Optional<WalkResult> result = callback.apply(operation);
          if (result.isPresent() && result.get() != WalkResult.CONTINUE) {
            return result;
          }
        }
      }
    }
    return Optional.empty();
  }
}

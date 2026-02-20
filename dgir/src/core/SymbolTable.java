package core;

import core.ir.Block;
import core.ir.Op;
import core.ir.Operation;
import core.ir.Region;
import core.traits.IOpTrait;
import core.traits.ISymbol;
import core.traits.ISymbolTable;
import dialect.builtin.attributes.StringAttribute;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * SymbolTable for ops which implement the ISymbolTable interface.
 * This allows managing the symbols of that op.
 */
public class SymbolTable {
  /**
   * Look up a symbol with the given name in the given operation. The operation must implement ISymbolTable.
   * The symbol table is searched in the first region of the operation, and only in the first block of that region.
   * If there are multiple regions or blocks, they are ignored.
   *
   * @param operation  The operation to look up the symbol in. Must implement ISymbolTable.
   * @param symbolName The name of the symbol to look up.
   * @return The operation that defines the symbol with the given name, or null if no such symbol exists in the first region and block of the given operation.
   * @throws AssertionError if the given operation does not implement ISymbolTable.
   */
  @Contract(pure = true)
  public static @Nullable Operation lookupSymbolIn(@NotNull Operation operation, @NotNull String symbolName) {
    assert operation.hasTrait(ISymbolTable.class);
    Region region = operation.getFirstRegion().orElseThrow(() -> new AssertionError("Operation does not have a region. Symbol tables must have at least one region."));
    if (region.getBlocks().isEmpty())
      return null;

    for (Operation op : region.getBlocks().getFirst().getOperations()) {
      Optional<String> name = getNameIfSymbol(op, getSymbolAttributeName());
      if (name.isPresent() && name.get().equals(symbolName)) {
        return op;
      }
    }
    return null;
  }

  /**
   * Get the symbol name of the given operation if it has the symbol attribute. The symbol attribute is defined by the
   * getSymbolAttributeName method.
   *
   * @param op                  Operation to get the symbol name from.
   * @param symbolAttributeName Name of the attribute that stores the symbol name.
   * @return Symbol name of the given operation, or null if the operation does not have the symbol attribute.
   */
  @Contract(pure = true)
  private static @NotNull Optional<String> getNameIfSymbol(@NotNull Operation op, @NotNull String symbolAttributeName) {
    var attr = op.getAttribute(StringAttribute.class, symbolAttributeName);
    return attr.map(StringAttribute::getValue);
  }

  /**
   * Look up the nearest symbol table from the given operation. The search starts from the given operation and goes up
   * the parent chain until a symbol table is found or the root is reached.
   *
   * @param from Operation from where to start the search.
   * @return An Optional containing the nearest symbol table operation if found, or an empty Optional if no symbol table
   * is found in the parent chain.
   * @throws AssertionError if the given operation is null.
   */
  @Contract(pure = true)
  public static @NotNull Optional<Operation> nearestSymbolTable(@NotNull Operation from) {
    if (from.hasTrait(ISymbolTable.class)) {
      return Optional.of(from);
    }

    var symbolTableOp = from.getParentWithTrait(ISymbolTable.class);
    return symbolTableOp.map(IOpTrait::get);
  }

  /**
   * Look up a symbol with the given name in the nearest symbol table from the given operation. The search starts from the given operation
   * and goes up the parent chain until a symbol table is found or the root is reached.
   *
   * @param from       Operation from where to start the search.
   * @param symbolName Name of the symbol to look up.
   * @return An Optional containing the operation that defines the symbol with the given name if found, or an empty Optional if no such symbol exists.
   */
  @Contract(pure = true)
  public static @NotNull Optional<Operation> lookupSymbolInNearestTable(@NotNull Operation from, @NotNull String symbolName) {
    Optional<Operation> symbolTableOp = nearestSymbolTable(from);
    if (symbolTableOp.isEmpty()) {
      return Optional.empty();
    }
    Operation foundOp = lookupSymbolIn(symbolTableOp.get(), symbolName);
    return Optional.ofNullable(foundOp);
  }

  /**
   * Look up a symbol with the given name in the nearest symbol table from the given operation and check if it is an
   * instance of the given class. The search starts from the given operation and goes up the parent chain until a symbol
   * table is found or the root is reached.
   *
   * @param from       Operation from where to start the search.
   * @param symbolName Name of the symbol to look up.
   * @param clazz      Class of the symbol to look up.
   * @param <T>        Type of the symbol to look up.
   * @return An Optional containing the operation that defines the symbol with the given name if found and is an instance of the given class, or an empty Optional if no such symbol exists.
   */
  @Contract(pure = true)
  public static <T extends Op & ISymbol> Optional<@NotNull T> lookupSymbolInNearestTableAsOp(@NotNull Operation from, @NotNull String symbolName, @NotNull Class<T> clazz) {
    Optional<Operation> foundOp = lookupSymbolInNearestTable(from, symbolName);
    if (foundOp.isEmpty() || !foundOp.get().isa(clazz)) {
      return Optional.empty();
    }
    return foundOp.get().as(clazz);
  }

  /**
   * Get the name of the attribute used to store the symbol name in operations that define symbols. This is used by the
   * lookupSymbolIn and related methods to find the symbol name attribute in operations.
   *
   * @return Name of the attribute used to store the symbol name.
   */
  @Contract(pure = true)
  public static String getSymbolAttributeName() {
    return "symbol_name";
  }
}

package blockly.dgir.compiler.SymbolTable;

import com.github.javaparser.utils.Pair;
import dgir.core.ir.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ScopedSymbolTable {
  /**
   * A stack of symbol tables representing the current scope. Each entry in the stack is a pair of a
   * boolean indicating whether the scope is isolated from above (i.e. whether symbols in this scope
   * are only visible through isolation) and a map from qualified mangled names to symbols defined
   * in that scope. When looking up a symbol, we search from the top of the stack downwards, and if
   * we encounter an isolated scope, we stop.
   */
  private final @NotNull Deque<
          @NotNull Pair<@NotNull Boolean, @NotNull Map<@NotNull String, @NotNull Value>>>
      scopedSymbols = new ArrayDeque<>();

  /**
   * A reference to the parent symbol table, which is used for nested tables. If the current table
   * is the root table, this will be null.
   */
  private final @Nullable ScopedSymbolTable parent;

  protected ScopedSymbolTable() {
    this.parent = null;
  }

  protected ScopedSymbolTable(@NotNull ScopedSymbolTable parent) {
    this.parent = parent;
  }

  public static ScopedSymbolTable createRoot() {
    return new ScopedSymbolTable();
  }

  public ScopedSymbolTable openNewTable() {
    return new ScopedSymbolTable(this);
  }

  public void pushScope(boolean isolatedFromAbove) {
    scopedSymbols.push(new Pair<>(isolatedFromAbove, new HashMap<>()));
  }

  public @NotNull Pair<@NotNull Boolean, @NotNull Map<@NotNull String, @NotNull Value>> popScope() {
    assert !scopedSymbols.isEmpty() : "Cannot pop from an empty scoped symbol table.";
    return scopedSymbols.pop();
  }

  public void insertScoped(@NotNull String name, @NotNull Value symbol) {
    assert !scopedSymbols.isEmpty() && scopedSymbols.peek().b != null
        : "Cannot insert symbol into an empty scoped symbol table. Open a new scope before inserting symbols.";
    scopedSymbols.peek().b.put(name, symbol);
  }

  public @NotNull Optional<Value> lookupScoped(@NotNull String name) {
    for (var scope : scopedSymbols) {
      if (scope.b.containsKey(name)) {
        Value symbol = scope.b.get(name);
        return Optional.of(symbol);
      }
      if (scope.a) {
        return Optional.empty();
      }
    }
    if (parent != null) {
      return parent.lookupScoped(name);
    }
    return Optional.empty();
  }
}

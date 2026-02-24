package core.traits;

import core.SymbolTable;
import dialect.builtin.attributes.StringAttribute;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Marks an operation as declaring a named symbol that can be looked up via {@link SymbolTable}.
 *
 * <p>The implementing op must carry an attribute named {@link SymbolTable#getSymbolAttributeName()}
 * (i.e. {@code "symbol_name"}) that holds the symbol's string name as a {@link StringAttribute}.
 *
 * <p>The verifier checks that the attribute is present. {@link #getSymbol()} is a convenience
 * accessor that reads the attribute value.
 *
 * <p>Examples: {@link dialect.func.FuncOp}.
 */
public interface ISymbol extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull ISymbol ignored) {
    if (!getOperation().getAttributes().containsKey(SymbolTable.getSymbolAttributeName())) {
      getOperation().emitError("Symbol must have a symbol attribute.");
      return false;
    }
    return true;
  }

  @Contract(pure = true)
  default @NotNull String getSymbol() {
    return getOperation()
        .getAttribute(StringAttribute.class, SymbolTable.getSymbolAttributeName())
        .orElseThrow()
        .getValue();
  }
}

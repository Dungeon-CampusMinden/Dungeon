package dialect.builtin.attributes;

import core.*;
import core.ir.Attribute;
import dialect.builtin.BuiltinDialect;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Attribute that holds a reference to a symbol by its string name.
 *
 * <p>Ident: {@code symbolRefAttr}. Used by operations such as {@link dialect.func.CallOp} to
 * record the name of a callee function without hard-linking the IR nodes together.
 */
public class SymbolRefAttribute extends Attribute {

  // =========================================================================
  // Static Fields
  // =========================================================================

  /** Prototype instance used during dialect registration. */
  public static final SymbolRefAttribute INSTANCE = new SymbolRefAttribute();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  @Contract(pure = true)
  public @NotNull String getIdent() {
    return "symbolRefAttr";
  }

  @Override
  @Contract(pure = true)
  public @NotNull String getNamespace() {
    return "";
  }

  @Override
  @Contract(pure = true)
  public @NotNull Class<? extends Dialect> getDialect() {
    return BuiltinDialect.class;
  }

  // =========================================================================
  // Members
  // =========================================================================

  /** The referenced symbol name. */
  private String value;

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Create a default symbol reference attribute with a {@code null} name. */
  public SymbolRefAttribute() {}

  /**
   * Create a symbol reference attribute pointing to the given name.
   *
   * @param value the symbol name to reference.
   */
  public SymbolRefAttribute(@NotNull String value) {
    this.value = value;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the referenced symbol name.
   *
   * @return the symbol name, or {@code null} if not set.
   */
  @Contract(pure = true)
  @Override
  public @Nullable String getStorage() {
    return value;
  }

  /**
   * Returns the referenced symbol name.
   *
   * @return the symbol name.
   */
  @Contract(pure = true)
  public String getValue() {
    return value;
  }

  /**
   * Updates the referenced symbol name.
   *
   * @param value the new symbol name.
   */
  public void setValue(@NotNull String value) {
    this.value = value;
  }
}

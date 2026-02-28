package dialect.func;

import core.SymbolTable;
import core.ir.NamedAttribute;
import core.ir.Operation;
import core.debug.Location;
import core.traits.IGlobal;
import core.traits.IIsolatedFromAbove;
import core.traits.ISingleRegion;
import core.traits.ISymbol;
import dialect.builtin.attributes.StringAttribute;
import dialect.builtin.attributes.TypeAttribute;
import dialect.func.types.FuncType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Declares a named function with a body region in the {@code func} dialect.
 *
 * <p>A {@code func.func} op carries two mandatory attributes:
 *
 * <ul>
 *   <li>{@link SymbolTable#getSymbolAttributeName()} — the function's symbol name (e.g. {@code
 *       "main"}).
 *   <li>{@code "type"} — a {@link TypeAttribute} wrapping the function's {@link FuncType}.
 * </ul>
 *
 * <p>The op contributes exactly one region that holds the function body. It implements {@link
 * ISymbol} so its name can be looked up via {@link SymbolTable}, and {@link IIsolatedFromAbove} to
 * prevent the body from capturing values defined outside the function.
 *
 * <p>MLIR reference: {@code func.func}
 *
 * <pre>{@code
 * func.func @add(%a: int32, %b: int32) -> int32 {
 *   ...
 * }
 * }</pre>
 */
public final class FuncOp extends FuncBaseOp
    implements Func, ISymbol, IIsolatedFromAbove, IGlobal, ISingleRegion {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull String getIdent() {
    return "func.func";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  @Contract(pure = true)
  @Override
  public @NotNull List<NamedAttribute> getDefaultAttributes() {
    return List.of(
        new NamedAttribute(SymbolTable.getSymbolAttributeName(), new StringAttribute("foo")),
        new NamedAttribute("type", new TypeAttribute(new FuncType())));
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  private FuncOp() {}

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public FuncOp(@NotNull Operation operation) {
    super(operation);
  }

  /**
   * Create a function with the given name and a default (no-arg, void) {@link FuncType}.
   *
   * @param location the source location of this operation.
   * @param name the symbol name of the function.
   */
  public FuncOp(@NotNull Location location, @NotNull String name) {
    this(location, name, new FuncType());
  }

  /**
   * Create a function with the given name and explicit type.
   *
   * @param location the source location of this operation.
   * @param name the symbol name of the function.
   * @param type the function signature.
   */
  public FuncOp(@NotNull Location location, @NotNull String name, @NotNull FuncType type) {
    setOperation(
        true, Operation.Create(location, this, null, null, type.getOutput(), type.getInputs()));
    getFuncNameAttribute().setValue(name);
    getTypeAttribute().setType(type);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the {@link StringAttribute} that holds the function's symbol name.
   *
   * @return the symbol name attribute.
   * @throws RuntimeException if the attribute is absent.
   */
  @Contract(pure = true)
  public @NotNull StringAttribute getFuncNameAttribute() {
    return getAttribute(SymbolTable.getSymbolAttributeName(), StringAttribute.class)
        .orElseThrow(() -> new RuntimeException("Symbol attribute not found"));
  }

  /**
   * Returns the function's symbol name as a plain string.
   *
   * @return the function name.
   */
  @Contract(pure = true)
  public @NotNull String getFuncName() {
    return getFuncNameAttribute().getValue();
  }

  /**
   * Returns the {@link TypeAttribute} that carries the function's {@link FuncType}.
   *
   * @return the type attribute.
   * @throws RuntimeException if the attribute is absent.
   */
  @Contract(pure = true)
  public @NotNull TypeAttribute getTypeAttribute() {
    return getOperation()
        .getAttribute(TypeAttribute.class, "type")
        .orElseThrow(() -> new RuntimeException("Type attribute not found"));
  }

  /**
   * Returns the function's {@link FuncType} (inputs + output).
   *
   * @return the function type.
   */
  @Contract(pure = true)
  public @NotNull FuncType getType() {
    return (FuncType) getTypeAttribute().getStorage();
  }
}

package dialect.func;

import core.SymbolTable;
import core.debug.Location;
import core.ir.*;
import core.traits.ISymbolUser;
import dialect.func.types.FuncType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static dialect.builtin.BuiltinAttrs.SymbolRefAttribute;

/**
 * Calls a named function in the {@code func} dialect.
 *
 * <p>The callee is referenced by name via the {@code "callee"} {@link SymbolRefAttribute}. At
 * verification time the symbol is resolved in the nearest enclosing {@link core.SymbolTable} and
 * the operand/result types are checked against the callee's {@link FuncType}.
 *
 * <p>MLIR reference: {@code func.call}
 *
 * <pre>{@code
 * %result = func.call @add(%a, %b) : (int32, int32) -> int32
 * }</pre>
 */
public final class CallOp extends FuncBaseOp implements Func, ISymbolUser {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull String getIdent() {
    return "func.call";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return operation -> {
      CallOp callOp = operation.as(CallOp.class).orElseThrow();

      // Make sure that the callee function exists in the nearest symbol table
      Optional<FuncOp> callee =
          SymbolTable.lookupSymbolInNearestTableAsOp(operation, callOp.getCallee(), FuncOp.class);
      if (callee.isEmpty()) {
        operation.emitError("Could not find function " + callOp.getCallee());
        return false;
      }

      // Make sure that the function type matches the call site
      var calleeType = callee.get().getType();
      var funcType = callOp.getFunctionType();
      if (!calleeType.equals(funcType)) {
        callOp
            .getOperation()
            .emitError(
                "Function type does not match call site type for function "
                    + callOp.getCallee()
                    + ": "
                    + calleeType.getParameterizedIdent()
                    + " != "
                    + funcType.getParameterizedIdent());
        return false;
      }
      return true;
    };
  }

  @Contract(pure = true)
  @Override
  public @NotNull List<NamedAttribute> getDefaultAttributes() {
    return List.of(new NamedAttribute(getCalleeAttributeName(), new SymbolRefAttribute("foo")));
  }

  /**
   * Returns the attribute name used to store the callee symbol reference.
   *
   * @return {@code "callee"}
   */
  @Contract(pure = true)
  public static @NotNull String getCalleeAttributeName() {
    return "callee";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  private CallOp() {}

  /**
   * Create a call with an explicit operand list and callee type.
   *
   * @param location    the source location of this operation.
   * @param name        the symbol name of the function to call.
   * @param operands    the argument values.
   * @param calleeType  the function signature used to determine the result type.
   */
  public CallOp(@NotNull Location location, @NotNull String name, @NotNull List<Value> operands, @NotNull FuncType calleeType) {
    setOperation(Operation.Create(location, this, operands, null, calleeType.getOutput()));
    setCallee(name);
  }

  /**
   * Create a call using varargs syntax.
   *
   * @param location    the source location of this operation.
   * @param name        the symbol name of the function to call.
   * @param calleeType  the function signature used to determine the result type.
   * @param operands    the argument values (varargs).
   */
  public CallOp(@NotNull Location location, @NotNull String name, @NotNull FuncType calleeType, Value... operands) {
    this(location, name, List.of(operands), calleeType);
  }

  /**
   * Create a call to a specific {@link FuncOp} with an explicit operand list.
   *
   * @param location the source location of this operation.
   * @param funcOp   the function to call.
   * @param operands the argument values.
   */
  public CallOp(@NotNull Location location, @NotNull FuncOp funcOp, @NotNull List<Value> operands) {
    setOperation(Operation.Create(location, this, operands, null, funcOp.getType().getOutput()));
    setCallee(funcOp.getFuncName());
  }

  /**
   * Create a call to a specific {@link FuncOp} using varargs syntax.
   *
   * @param location the source location of this operation.
   * @param funcOp   the function to call.
   * @param operands the argument values (varargs).
   */
  public CallOp(@NotNull Location location, @NotNull FuncOp funcOp, Value... operands) {
    this(location, funcOp, List.of(operands));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the symbol name of the callee function.
   *
   * @return the callee symbol name.
   */
  @Contract(pure = true)
  public @NotNull String getCallee() {
    return Objects.requireNonNull(
        getAttribute(getCalleeAttributeName(), SymbolRefAttribute.class)
            .orElseThrow(() -> new AssertionError("No callee attribute found"))
            .getStorage(),
        "Callee symbol name must not be null");
  }

  private void setCallee(@NotNull String name) {
    getSymbolRefAttribute().setValue(name);
  }

  /**
   * Get the function type that results from this call's operands and output.
   *
   * @return The function type inferred from the operands and the operation result.
   */
  @Contract(pure = true)
  public @NotNull FuncType getFunctionType() {
    List<Type> inputTypes =
        getOperands().stream().map(ValueOperand::getType).map(type -> type.orElse(null)).toList();
    Type outputType = getOutput().map(OperationResult::getType).orElse(null);
    return new FuncType(inputTypes, outputType);
  }

  @Contract(pure = true)
  @Override
  public @NotNull SymbolRefAttribute getSymbolRefAttribute() {
    return getAttribute(getCalleeAttributeName(), SymbolRefAttribute.class)
        .orElseThrow(() -> new RuntimeException("No symbol attribute found"));
  }
}

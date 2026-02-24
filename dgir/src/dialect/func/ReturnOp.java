package dialect.func;

import core.ir.Op;
import core.ir.Operation;
import core.ir.Value;
import core.traits.ISpecificParentOp;
import core.traits.ITerminator;
import core.traits.IZeroOrOneOperand;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Returns from a {@link FuncOp}, optionally carrying a single return value.
 *
 * <p>This is a terminator: it must be the last operation in the function body's exit block. If the
 * enclosing {@link FuncOp} has a non-void output type, exactly one operand must be provided and its
 * type must match the function output type.
 *
 * <p>MLIR reference: {@code func.return}
 *
 * <pre>{@code
 * func.return %result : int32
 * // or, for void functions:
 * func.return
 * }</pre>
 */
public final class ReturnOp extends FuncBaseOp implements Func, ITerminator, IZeroOrOneOperand, ISpecificParentOp {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull String getIdent() {
    return "func.return";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return operation -> {
      ReturnOp returnOp = operation.as(ReturnOp.class).orElseThrow();

      // Ensure that the parent operation is a func.func op
      Optional<Operation> parentOp = operation.getParentOperation();
      if (parentOp.isEmpty()) {
        operation.emitError("Return operation must be nested in a function");
        return false;
      }
      Optional<FuncOp> parentFuncOp = parentOp.get().as(FuncOp.class);
      if (parentFuncOp.isEmpty()) {
        operation.emitError("Return operation must be nested in a function");
        return false;
      }
      // Ensure that the return op's operand type matches the function output type
      if (returnOp.getOperandType().isPresent()) {
        var returnType =
            returnOp
                .getOperandType()
                .get()
                .orElseThrow(() -> new RuntimeException("Return op operand value is not set."));
        var funcType = parentFuncOp.get().getType();
        if (!returnType.equals(funcType.getOutput())) {
          operation.emitError(
              "Return type "
                  + returnType.getParameterizedIdent()
                  + " does not match function return type "
                  + (funcType.getOutput() != null
                      ? funcType.getOutput().getParameterizedIdent()
                      : null));
          return false;
        }
      }
      return true;
    };
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Default constructor used during dialect registration. */
  public ReturnOp() {
    executeIfRegistered(
        ReturnOp.class, () -> setOperation(false, Operation.Create(this, null, null, null)));
  }

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public ReturnOp(@NotNull Operation operation) {
    super(operation);
  }

  /**
   * Create a return op that yields the given value.
   *
   * @param operand the value to return from the enclosing function.
   */
  public ReturnOp(@NotNull Value operand) {
    setOperation(Operation.Create(this, List.of(operand), null, null));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Class<? extends Op>> getValidParentTypes() {
    return List.of(FuncOp.class);
  }

  /**
   * Returns the value being returned, or empty if this is a void return.
   *
   * @return the optional return value.
   */
  @Contract(pure = true)
  public @NotNull Optional<Value> getReturnValue() {
    return getOperand().flatMap(value -> value);
  }
}

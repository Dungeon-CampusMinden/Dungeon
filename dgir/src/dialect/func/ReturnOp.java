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

public final class ReturnOp extends FuncBaseOp implements Func, ITerminator, IZeroOrOneOperand, ISpecificParentOp {

  // =========================================================================
  // Type Info
  // =========================================================================

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

  public ReturnOp() {
    executeIfRegistered(
        ReturnOp.class, () -> setOperation(false, Operation.Create(this, null, null, null)));
  }

  public ReturnOp(Operation operation) {
    super(operation);
  }

  public ReturnOp(Value operand) {
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

  public @NotNull Optional<Value> getReturnValue() {
    return getOperand().flatMap(value -> value);
  }
}

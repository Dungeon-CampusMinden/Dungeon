package dialect.func;

import core.*;
import core.detail.OperationDetails;
import core.ir.*;
import core.traits.ISpecificParentOp;
import core.traits.ITerminator;
import core.traits.IZeroOrOneOperand;
import dialect.builtin.Builtin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ReturnOp extends Op implements ITerminator, IZeroOrOneOperand, ISpecificParentOp {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public OperationDetails.Impl createDetails() {
    class ReturnOpModel extends OperationDetails.Impl {
      ReturnOpModel() {
        super(ReturnOp.getIdent(), ReturnOp.class, DGIRContext.registeredDialects.get(Builtin.class), List.of());
      }

      @Override
      public boolean verify(Operation operation) {
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
          var returnType = returnOp
            .getOperandType().get()
            .orElseThrow(() -> new RuntimeException("Return op operand value is not set."));
          var funcType = parentFuncOp.get().getType();
          if (!returnType.equals(funcType.getOutput())) {
            operation.emitError("Return type " + returnType.getParameterizedIdent()
              + " does not match function return type "
              + (funcType.getOutput() != null ? funcType.getOutput().getParameterizedIdent() : null));
            return false;
          }
        }
        return true;
      }

      @Override
      public void populateDefaultAttrs(List<NamedAttribute> attributes) {
      }
    }
    return new ReturnOpModel();
  }

  public static String getIdent() {
    return "func.return";
  }

  public static String getNamespace() {
    return "func";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public ReturnOp() {
    executeIfRegistered(ReturnOp.class, () ->
      setOperation(false, Operation.Create(getIdent(), null, null, null)));
  }

  public ReturnOp(Operation operation) {
    super(operation);
  }

  public ReturnOp(Value operand) {
    super(Operation.Create(getIdent(), List.of(operand), null, null));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Override
  public List<Class<? extends Op>> getValidParentTypes() {
    return List.of(FuncOp.class);
  }

  public @NotNull Optional<Value> getReturnValue() {
    return getOperand().flatMap(value -> value);
  }
}

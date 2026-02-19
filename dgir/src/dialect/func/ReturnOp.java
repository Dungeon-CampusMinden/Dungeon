package dialect.func;

import core.*;
import core.detail.OperationDetails;
import core.detail.RegisteredOperationDetails;
import core.ir.NamedAttribute;
import core.ir.Op;
import core.ir.Operation;
import core.ir.Value;
import core.traits.ISingleOperand;
import core.traits.ISpecificParentOp;
import core.traits.ITerminator;
import core.traits.IZeroOrOneOperand;
import dialect.builtin.Builtin;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ReturnOp extends Op implements ITerminator, IZeroOrOneOperand, ISpecificParentOp {
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
        // Ensure that the return ops operand type is the same as the function output type if there is an operand
        if (returnOp.getOperand().isPresent()) {
          var returnType = returnOp.getOperandType().orElseThrow();
          var funcType = parentFuncOp.get().getType();
          if (!returnType.equals(funcType.getOutput())) {
            operation.emitError("Return type " + returnType.getParameterizedIdent() + " does not match function return type " + (funcType.getOutput() != null ? funcType.getOutput().getParameterizedIdent() : null));
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

  public static String getIdent() {
    return "func.return";
  }

  public static String getNamespace() {
    return "func";
  }

  @Override
  public List<Class<? extends Op>> getValidParentTypes() {
    return List.of(FuncOp.class);
  }
}

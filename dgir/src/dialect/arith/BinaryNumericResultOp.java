package dialect.arith;

import core.ir.Operation;
import core.ir.OperationResult;
import core.traits.IBinaryOperands;
import core.traits.IHasResult;

import java.util.function.Function;

/** Base class for binary numeric ops that return the dominant operand type. */
public abstract class BinaryNumericResultOp extends BinaryNumericOp implements IHasResult {

  /** Default constructor used during dialect registration. */
  BinaryNumericResultOp() {
    super();
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return operation -> {
      if (!verifyBinaryNumericOperands(operation)) {
        return false;
      }
      var binaryOp = operation.asTrait(IBinaryOperands.class).orElseThrow();
      if (operation.getOutput().isEmpty()) {
        operation.emitError("Operation must have an output");
        return false;
      }
      var lhsType = binaryOp.getLhs().getType();
      var rhsType = binaryOp.getRhs().getType();
      var expectedType = getDominantType(lhsType, rhsType);
      var actualType = operation.getOutput().map(OperationResult::getType).orElseThrow();
      if (!actualType.equals(expectedType)) {
        operation.emitError("Result type must be the dominant operand type");
        return false;
      }
      return true;
    };
  }
}

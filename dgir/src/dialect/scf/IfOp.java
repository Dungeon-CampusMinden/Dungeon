package dialect.scf;

import core.Utils;
import core.ir.*;
import core.traits.IControlFlow;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Op which represents an if statement. It has one region for the "then" block and optionally one
 * region for the "else" block.
 */
public final class IfOp extends ScfOp implements SCF, IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "scf.if";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return operation -> {
      // Make sure the operations condition is of type int1
      Optional<Value> condOpt = operation.getOperandValue(0);
      if (condOpt.isEmpty()) {
        operation.emitError("Condition operand is missing");
        return false;
      }

      if (!condOpt.get().getType().equals(IntegerT.BOOL)) {
        operation.emitError("Condition operand must be of type int1");
        return false;
      }
      return true;
    };
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  private IfOp() {}

  public IfOp(Operation operation) {
    super(operation);
  }

  public IfOp(Value condition, boolean withElseBlock) {
    setOperation(
        Operation.Create(this, List.of(condition), null, null, withElseBlock ? 2 : 1));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public Region getThenRegion() {
    return getRegions().getFirst();
  }

  public Optional<Region> getElseRegion() {
    if (getRegions().size() == 1) return Optional.empty();
    return Optional.of(getRegions().get(1));
  }
}

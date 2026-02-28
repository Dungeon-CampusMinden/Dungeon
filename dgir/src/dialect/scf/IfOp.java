package dialect.scf;

import core.ir.*;
import core.debug.Location;
import core.traits.IControlFlow;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Conditional operation in the {@code scf} dialect.
 *
 * <p>The condition operand must be of type {@link IntegerT#BOOL} ({@code int1}). The op has one
 * mandatory {@code then} region and an optional {@code else} region. Control enters the then-region
 * when the condition is {@code true} ({@code 1}) and the else-region (if present) when it is
 * {@code false} ({@code 0}).
 *
 * <p>Ident: {@code scf.if}
 *
 * <pre>{@code
 * scf.if %cond {
 *   // then body
 * } else {
 *   // else body (optional)
 * }
 * }</pre>
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

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public IfOp(Operation operation) {
    super(operation);
  }

  /**
   * Create an if-op with the given boolean condition.
   *
   * @param location      the source location of this operation.
   * @param condition     a {@link IntegerT#BOOL} value controlling the branch.
   * @param withElseBlock {@code true} to also create an else region.
   */
  public IfOp(@NotNull Location location, Value condition, boolean withElseBlock) {
    setOperation(
        Operation.Create(location, this, List.of(condition), null, null, withElseBlock ? 2 : 1));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the then-region of this if-op.
   *
   * @return the first region.
   */
  public Region getThenRegion() {
    return getRegions().getFirst();
  }

  /**
   * Returns the else-region of this if-op, if present.
   *
   * @return the second region, or empty if no else branch was created.
   */
  public Optional<Region> getElseRegion() {
    if (getRegions().size() == 1) return Optional.empty();
    return Optional.of(getRegions().get(1));
  }
}

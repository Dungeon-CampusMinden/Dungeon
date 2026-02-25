package dialect.cf;

import core.ir.Block;
import core.ir.Operation;
import core.ir.Location;
import core.ir.Value;
import core.traits.IControlFlow;
import core.traits.ITerminator;
import dialect.builtin.types.IntegerT;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Conditional branch that selects between two target blocks based on a boolean condition.
 *
 * <p>This is a terminator: it must be the last operation in its parent block. Control is
 * transferred to {@code target} if the condition is {@code true} ({@code 1}), or to
 * {@code elseTarget} if the condition is {@code false} ({@code 0}).
 *
 * <p>The condition operand must be of type {@link IntegerT#BOOL} ({@code int1}).
 *
 * <p>MLIR reference: {@code cf.br_cond}
 *
 * <pre>{@code
 * cf.br_cond %cond, ^trueBlock, ^falseBlock
 * }</pre>
 */
public final class BranchCondOp extends CfOp implements CF, ITerminator, IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull String getIdent() {
    return "cf.br_cond";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  private BranchCondOp() {}

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public BranchCondOp(@NotNull Operation operation) {
    super(operation);
  }

  /**
   * Create a conditional branch.
   *
   * @param location   the source location of this operation.
   * @param condition  an {@link IntegerT#BOOL} value controlling the branch direction.
   * @param target     the successor block taken when {@code condition} is {@code true}.
   * @param elseTarget the successor block taken when {@code condition} is {@code false}.
   */
  public BranchCondOp(@NotNull Location location, @NotNull Value condition, @NotNull Block target, @NotNull Block elseTarget) {
    setOperation(Operation.Create(location, this, List.of(condition), List.of(target, elseTarget), null));
    assert condition.getType().equals(IntegerT.BOOL) : "Condition must be of type bool/int1.";
  }
}

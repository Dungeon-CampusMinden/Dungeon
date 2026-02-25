package dialect.cf;

import core.ir.Block;
import core.ir.Operation;
import core.ir.SourceLocation;
import core.traits.IControlFlow;
import core.traits.ITerminator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Unconditional branch to a single target {@link Block}.
 *
 * <p>This is a terminator: it must be the last operation in its parent block, and it transfers
 * control unconditionally to the specified successor.
 *
 * <p>MLIR reference: {@code cf.br}
 *
 * <pre>{@code
 * cf.br ^target
 * }</pre>
 */
public final class BranchOp extends CfOp implements CF, ITerminator, IControlFlow {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @NotNull String getIdent() {
    return "cf.br";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  private BranchOp() {}

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public BranchOp(@NotNull Operation operation) {
    super(operation);
  }

  /**
   * Create an unconditional branch to {@code target}.
   *
   * @param location the source location of this operation.
   * @param target the successor block to branch to.
   */
  public BranchOp(@NotNull SourceLocation location, @NotNull Block target) {
    setOperation(Operation.Create(location, this, null, List.of(target), null));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the target block this branch jumps to.
   *
   * @return the successor block.
   */
  @Contract(pure = true)
  public @NotNull Block getTarget() {
    return getSuccessors().getFirst();
  }
}

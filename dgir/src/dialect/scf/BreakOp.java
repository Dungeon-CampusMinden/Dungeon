package dialect.scf;

import core.ir.Op;
import core.ir.Operation;
import core.ir.Location;
import core.traits.ISpecificParentOp;
import core.traits.ITerminator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Function;

/**
 * Breaks out of the nearest enclosing {@link ForOp} in the {@code scf} dialect.
 *
 * <p>This is a terminator; it must be the last operation in its parent block. It is only valid
 * when directly nested inside a {@link ForOp} body (enforced by {@link ISpecificParentOp}).
 *
 * <p>Ident: {@code scf.break}
 *
 * <pre>{@code
 * scf.for (%i = ...) {
 *   scf.if %cond {
 *     scf.break
 *   }
 *   scf.continue
 * }
 * }</pre>
 */
public final class BreakOp extends ScfOp implements SCF, ITerminator, ISpecificParentOp {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "scf.break";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Default constructor used during dialect registration. */
  private BreakOp() {
    executeIfRegistered(
        BreakOp.class, () -> setOperation(false, Operation.Create(Location.UNKNOWN, this, null, null, null)));
  }

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public BreakOp(Operation operation) {
    super(operation);
  }

  /**
   * Create a break op.
   *
   * @param location the source location of this operation.
   */
  public BreakOp(@NotNull Location location) {
    setOperation(false, Operation.Create(location, this, null, null, null));
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the only valid parent op type: {@link ForOp}.
   *
   * @return an unmodifiable singleton list containing {@link ForOp}.
   */
  @Override
  public @NotNull @Unmodifiable List<Class<? extends Op>> getValidParentTypes() {
    return List.of(ForOp.class);
  }
}

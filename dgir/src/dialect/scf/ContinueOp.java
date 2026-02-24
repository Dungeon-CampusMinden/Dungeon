package dialect.scf;

import core.ir.Op;
import core.ir.Operation;
import core.traits.ISpecificParentOp;
import core.traits.ITerminator;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Marks the end of a structured control-flow region body in the {@code scf} dialect.
 *
 * <p>This is a terminator that completes the current iteration of a loop or the body of a
 * conditional. It is valid inside {@link IfOp}, {@link ScopeOp}, and {@link ForOp} bodies
 * (enforced by {@link ISpecificParentOp}).
 *
 * <p>Ident: {@code scf.continue}
 *
 * <pre>{@code
 * scf.for (%i = ...) {
 *   // ... body ...
 *   scf.continue
 * }
 * }</pre>
 */
public final class ContinueOp extends ScfOp implements SCF, ITerminator, ISpecificParentOp {

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull String getIdent() {
    return "scf.continue";
  }

  @Override
  public Function<Operation, Boolean> getVerifier() {
    return ignored -> true;
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  /** Default constructor; creates a backing operation when the dialect is already registered. */
  public ContinueOp() {
    executeIfRegistered(
        ContinueOp.class, () -> setOperation(true, Operation.Create(this, null, null, null)));
  }

  /**
   * Wrapping constructor that binds this op to an existing backing {@link Operation}.
   *
   * @param operation the backing operation state.
   */
  public ContinueOp(Operation operation) {
    super(operation);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  /**
   * Returns the valid parent op types: {@link IfOp}, {@link ScopeOp}, and {@link ForOp}.
   *
   * @return an unmodifiable list of the three permitted parent classes.
   */
  @Contract(pure = true)
  @Override
  public @NotNull @Unmodifiable List<Class<? extends Op>> getValidParentTypes() {
    return List.of(IfOp.class, ScopeOp.class, ForOp.class);
  }
}

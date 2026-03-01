package dialect.scf;

import core.Dialect;
import core.debug.Location;
import core.ir.Op;
import core.ir.Operation;
import core.ir.Region;
import core.ir.Value;
import core.traits.IControlFlow;
import core.traits.ISingleRegion;
import core.traits.ISpecificParentOp;
import core.traits.ITerminator;
import dialect.builtin.BuiltinTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Sealed marker interface for all operations in the {@link ScfDialect}.
 *
 * <p>Every concrete op must both extend {@link ScfOp} and implement this interface so that {@link
 * core.Utils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface ScfOps {
  /**
   * Abstract base class for all operations in the {@code scf} (structured control flow) dialect.
   *
   * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
   * implement {@link ScfOps} to be enumerated by {@link ScfDialect}.
   */
  abstract class ScfOp extends Op {

    // =========================================================================
    // Op Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return ScfDialect.class;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getNamespace() {
      return "scf";
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Default constructor used during dialect registration. */
    ScfOp() {
      super();
    }
  }

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
  final class BreakOp extends ScfOp implements ScfOps, ITerminator, ISpecificParentOp {

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
          BreakOp.class,
          () -> setOperation(false, Operation.Create(Location.UNKNOWN, this, null, null, null)));
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
  final class ContinueOp extends ScfOp implements ScfOps, ITerminator, ISpecificParentOp {

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

    /** Default constructor used during dialect registration. */
    private ContinueOp() {
      executeIfRegistered(
          ContinueOp.class,
          () -> setOperation(true, Operation.Create(Location.UNKNOWN, this, null, null, null)));
    }

    /**
     * Create a continue op.
     *
     * @param location the source location of this operation.
     */
    public ContinueOp(@NotNull Location location) {
      setOperation(true, Operation.Create(location, this, null, null, null));
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

  /**
   * Counted for-loop in the {@code scf} dialect.
   *
   * <p>The loop body occupies a single region. The region receives four body values that are
   * accessible inside the body as the loop parameters:
   *
   * <ol>
   *   <li>the induction variable (current iteration value, {@code int32})
   *   <li>the lower bound (inclusive)
   *   <li>the upper bound (exclusive)
   *   <li>the step size
   * </ol>
   *
   * <p>The four operands passed at construction time seed the initial induction variable, lower
   * bound, upper bound, and step respectively.
   *
   * <p>Ident: {@code scf.for}
   *
   * <pre>{@code
   * scf.for (%i = %init to %upper step %step) {
   *   ...
   * }
   * }</pre>
   */
  final class ForOp extends ScfOp implements ScfOps, IControlFlow, ISingleRegion {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    public @NotNull String getIdent() {
      return "scf.for";
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return ignored -> true;
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    private ForOp() {}

    /**
     * Create a for-loop with the given loop parameters.
     *
     * @param location the source location of this operation.
     * @param initValue the initial value of the induction variable.
     * @param lowerBound the lower bound of the loop (inclusive).
     * @param upperBound the upper bound of the loop (exclusive).
     * @param step the step size per iteration.
     */
    public ForOp(
        @NotNull Location location,
        Value initValue,
        Value lowerBound,
        Value upperBound,
        Value step) {
      setOperation(
          true,
          Operation.Create(
              location,
              this,
              List.of(initValue, lowerBound, upperBound, step),
              null,
              null,
              List.of(BuiltinTypes.IntegerT.INT32)));
    }

    // =========================================================================
    // Functions
    // =========================================================================

    /**
     * Returns the induction variable for the loop body.
     *
     * @return the body value at index 0.
     */
    @Contract(pure = true)
    public Value getInductionValue() {
      return getRegion().getBodyValue(0).orElseThrow();
    }

    /**
     * Returns the lower bound value (operand 1) visible inside the loop body.
     *
     * @return the body value at index 1.
     */
    @Contract(pure = true)
    public Value getLowerBound() {
      return getRegion().getBodyValue(1).orElseThrow();
    }

    /**
     * Returns the upper bound value (operand 2) visible inside the loop body.
     *
     * @return the body value at index 2.
     */
    @Contract(pure = true)
    public Value getUpperBound() {
      return getRegion().getBodyValue(2).orElseThrow();
    }

    /**
     * Returns the step value (operand 3) visible inside the loop body.
     *
     * @return the body value at index 3.
     */
    @Contract(pure = true)
    public Value getStep() {
      return getRegion().getBodyValue(3).orElseThrow();
    }
  }

  /**
   * Conditional operation in the {@code scf} dialect.
   *
   * <p>The condition operand must be of type {@link BuiltinTypes.IntegerT#BOOL} ({@code int1}). The
   * op has one mandatory {@code then} region and an optional {@code else} region. Control enters
   * the then-region when the condition is {@code true} ({@code 1}) and the else-region (if present)
   * when it is {@code false} ({@code 0}).
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
  final class IfOp extends ScfOp implements ScfOps, IControlFlow {

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

        if (!condOpt.get().getType().equals(BuiltinTypes.IntegerT.BOOL)) {
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
     * Create an if-op with the given boolean condition.
     *
     * @param location the source location of this operation.
     * @param condition a {@link BuiltinTypes.IntegerT#BOOL} value controlling the branch.
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

  /**
   * Opens a new lexical scope in the {@code scf} dialect.
   *
   * <p>A {@code ScopeOp} has no semantic effect other than restricting the visibility of values
   * defined inside it — they are not accessible outside the scope's single region. The verifier
   * always passes.
   *
   * <p>Ident: {@code scf.scope}
   *
   * <pre>{@code
   * scf.scope {
   *   // variables declared here are not visible outside
   * }
   * }</pre>
   */
  final class ScopeOp extends ScfOp implements ScfOps, ISingleRegion, IControlFlow {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    public @NotNull String getIdent() {
      return "scf.scope";
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return ignored -> true;
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Default constructor used during dialect registration. */
    private ScopeOp() {}

    /**
     * Create a scope op.
     *
     * @param location the source location of this operation.
     */
    public ScopeOp(@NotNull Location location) {
      setOperation(true, Operation.Create(location, this, null, null, null, 1));
    }
  }
}

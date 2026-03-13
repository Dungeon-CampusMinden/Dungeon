package dgir.dialect.scf;

import dgir.core.Dialect;
import dgir.core.Utils;
import dgir.core.debug.Location;
import dgir.core.ir.*;
import dgir.core.traits.IControlFlow;
import dgir.core.traits.ISingleRegion;
import dgir.core.traits.ISpecificParentOp;
import dgir.core.traits.ITerminator;
import dgir.dialect.builtin.BuiltinTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Sealed marker interface for all operations in the {@link ScfDialect}.
 *
 * <p>Every concrete op must both extend {@link ScfOp} and implement this interface so that {@link
 * Utils.Dialect#allOps} can discover it automatically via reflection.
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
   * Explicit scope/branch terminator for SCF constructs.
   *
   * <p>Ident: {@code scf.end}
   */
  final class EndOp extends ScfOp implements ScfOps, ITerminator, ISpecificParentOp {

    /** {@inheritDoc} */
    @Override
    public @NotNull String getIdent() {
      return "scf.end";
    }

    /**
     * Verifier for {@code scf.end}.
     *
     * <p>Structural placement constraints are enforced via {@link ISpecificParentOp}; therefore
     * this verifier itself is a no-op.
     */
    @Override
    public @NotNull Function<@NotNull Operation, @NotNull Boolean> getVerifier() {
      return ignored -> true;
    }

    /**
     * Restricts {@code scf.end} to direct parents that represent explicit region boundaries in this
     * dialect.
     */
    @Override
    public @NotNull @Unmodifiable List<Class<? extends Op>> getValidParentTypes() {
      return List.of(IfOp.class, ScopeOp.class, WhileOp.class, ForOp.class);
    }

    /**
     * Returns the required single-{@link Location} constructor so this terminator can be
     * materialized by generic IR utilities.
     */
    @Override
    public @NotNull Optional<Constructor<? extends ITerminator>> getLocationConstructor() {
      try {
        return Optional.of(getClass().getConstructor(Location.class));
      } catch (NoSuchMethodException e) {
        throw new AssertionError(
            "Terminator "
                + getClass()
                + " does not define a public constructor that takes only a location as parameter.",
            e);
      }
    }

    /** Default constructor used during dialect registration. */
    private EndOp() {}

    /**
     * Create an explicit {@code scf.end} terminator.
     *
     * @param location the source location of this operation.
     */
    public EndOp(@NotNull Location location) {
      setOperation(true, Operation.Create(location, this, null, null, null));
    }
  }

  /**
   * Marks the end of a structured control-flow region body in the {@code scf} dialect.
   *
   * <p>This is a terminator that tells a loop to proceed to the next iteration (in the case of a
   * for-loop) or to re-evaluate its condition (in the case of a while-loop).
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
    public @NotNull Function<Operation, Boolean> getVerifier() {
      return ignored -> true;
    }

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Default constructor used during dialect registration. */
    private ContinueOp() {}

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
     * Returns the valid parent op types: {@link ForOp} and {@link WhileOp}.
     *
     * @return an unmodifiable list of the three permitted parent classes.
     */
    @Contract(pure = true)
    @Override
    public @NotNull @Unmodifiable List<Class<? extends Op>> getValidParentTypes() {
      return List.of(ForOp.class, WhileOp.class);
    }

    @Override
    public @NotNull Optional<Constructor<? extends ITerminator>> getLocationConstructor() {
      try {
        return Optional.of(getClass().getConstructor(Location.class));
      } catch (NoSuchMethodException e) {
        throw new AssertionError(
            "Terminator "
                + getClass()
                + " does not define a public constructor that takes only a location as parameter.",
            e);
      }
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
   * <p>Additionally, a "break" value is available as the second body value of the loop's single
   * region, which can be set to true to terminate the loop early (similar to "break" in most
   * languages) regardless of the loop condition.
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
    public @NotNull Function<Operation, Boolean> getVerifier() {
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
              List.of(
                  BuiltinTypes.IntegerT.INT32,
                  BuiltinTypes.IntegerT.BOOL,
                  BuiltinTypes.IntegerT.BOOL)));
    }

    // =========================================================================
    // Functions
    // =========================================================================

    /**
     * Returns the induction variable of this loop, which is the first body value of its single
     * region.
     *
     * @return the induction variable.
     */
    @Contract(pure = true)
    public @NotNull Value getInductionValue() {
      return getRegion().getBodyValue(0).orElseThrow();
    }

    /**
     * Returns operand 0: the initial value used to seed the induction variable before loop entry.
     *
     * @return the initial induction value operand.
     */
    @Contract(pure = true)
    public @NotNull Value getInitialValue() {
      return getOperand(0).flatMap(Operand::getValue).orElseThrow();
    }

    /**
     * Returns operand 1: the inclusive lower-bound value used by the loop.
     *
     * @return the lower-bound operand value.
     */
    @Contract(pure = true)
    public @NotNull Value getLowerBound() {
      return getOperand(1).flatMap(Operand::getValue).orElseThrow();
    }

    /**
     * Returns operand 2: the exclusive upper-bound value used by the loop.
     *
     * @return the upper-bound operand value.
     */
    @Contract(pure = true)
    public @NotNull Value getUpperBound() {
      return getOperand(2).flatMap(Operand::getValue).orElseThrow();
    }

    /**
     * Returns operand 3: the per-iteration step value.
     *
     * @return the step operand value.
     */
    @Contract(pure = true)
    public @NotNull Value getStep() {
      return getOperand(3).flatMap(Operand::getValue).orElseThrow();
    }

    /**
     * Returns the break value that can be set to terminate the loop early (similar to "break" in
     * most languages).
     *
     * @return the break value.
     */
    @Contract(pure = true)
    public @NotNull Value getBreakValue() {
      return getRegion().getBodyValue(1).orElseThrow();
    }

    /**
     * Returns the skip value that can be set to skip the rest of the current iteration and proceed
     * to the next one (similar to "continue" in most languages).
     *
     * @return the skip value.
     */
    @Contract(pure = true)
    public @NotNull Value getSkipValue() {
      return getRegion().getBodyValue(2).orElseThrow();
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
  final class IfOp extends ScfOp implements ScfOps, IControlFlow, ImplicitTerminator {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    public @NotNull String getIdent() {
      return "scf.if";
    }

    @Override
    public @NotNull Function<Operation, Boolean> getVerifier() {
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

    @Override
    public @NotNull Constructor<? extends ITerminator> getImplicitTerminatorType() {
      return new EndOp()
          .getLocationConstructor()
          .orElseThrow(
              () ->
                  new AssertionError("EndOp must have a public constructor that takes a location"));
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
  final class ScopeOp extends ScfOp
      implements ScfOps, ImplicitTerminator, ISingleRegion, IControlFlow {

    // =========================================================================
    // Type Info
    // =========================================================================

    @Override
    public @NotNull String getIdent() {
      return "scf.scope";
    }

    @Override
    public @NotNull Function<Operation, Boolean> getVerifier() {
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

    @Override
    public @NotNull Constructor<? extends ITerminator> getImplicitTerminatorType() {
      return new EndOp()
          .getLocationConstructor()
          .orElseThrow(
              () ->
                  new AssertionError(
                      "ContinueOp must have a public constructor that takes a location"));
    }
  }

  /**
   * Structured while-loop in the {@code scf} dialect.
   *
   * <p>The op owns two regions:
   *
   * <ol>
   *   <li><b>condition region</b> ({@link #getConditionRegion()}) that decides whether to continue
   *   <li><b>body region</b> ({@link #getBodyRegion()}) containing loop-body operations
   * </ol>
   *
   * <p>Setting the break value to {@code true} will terminate the loop early even if the condition
   * region would otherwise evaluate to true, allowing constructs like "break" in most languages.
   *
   * <p>Ident: {@code scf.while}
   */
  final class WhileOp extends ScfOp implements ScfOps, ImplicitTerminator, IControlFlow {
    /** {@inheritDoc} */
    @Override
    public @NotNull String getIdent() {
      return "scf.while";
    }

    /**
     * Verifier for {@code scf.while}.
     *
     * <p>No additional structural/type checks are performed here beyond generic IR invariants.
     */
    @Override
    public @NotNull Function<Operation, Boolean> getVerifier() {
      return ignored -> true;
    }

    /** Default constructor used during dialect registration. */
    private WhileOp() {}

    /**
     * Create a while-loop with two regions (condition and body).
     *
     * @param location the source location of this operation.
     */
    public WhileOp(@NotNull Location location) {
      setOperation(
          true,
          Operation.Create(
              location,
              this,
              null,
              null,
              null,
              List.of(),
              List.of(BuiltinTypes.IntegerT.BOOL, BuiltinTypes.IntegerT.BOOL)));
    }

    /**
     * Returns region 0, the condition/control region.
     *
     * @return the while-loop condition region.
     */
    @Contract(pure = true)
    public @NotNull Region getConditionRegion() {
      return getRegion(0).orElseThrow();
    }

    /**
     * Returns region 1, the body region.
     *
     * @return the while-loop body region.
     */
    @Contract(pure = true)
    public @NotNull Region getBodyRegion() {
      return getRegion(1).orElseThrow();
    }

    /**
     * Returns the value that can be set if the loop should terminate early (set by calling break in
     * most languages).
     *
     * @return the break value.
     */
    @Contract(pure = true)
    public @NotNull Value getBreakValue() {
      return getRegion(1).flatMap(region -> region.getBodyValue(0)).orElseThrow();
    }

    /**
     * Returns the value that can be set to skip the rest of the current iteration and proceed to
     * the next one (set by calling continue in most languages).
     *
     * @return the skip value.
     */
    @Contract(pure = true)
    public @NotNull Value getSkipValue() {
      return getRegion(1).flatMap(region -> region.getBodyValue(1)).orElseThrow();
    }

    /**
     * Declares the implicit terminator type used by while regions when one is not present
     * explicitly.
     *
     * @return constructor for {@link EndOp}.
     */
    @Override
    public @NotNull Constructor<? extends ITerminator> getImplicitTerminatorType() {
      return new ContinueOp().getLocationConstructor().orElseThrow();
    }
  }
}

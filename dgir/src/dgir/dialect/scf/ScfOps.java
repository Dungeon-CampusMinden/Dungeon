package dgir.dialect.scf;

import dgir.core.Dialect;
import dgir.core.Utils;
import dgir.core.debug.Location;
import dgir.core.ir.*;
import dgir.core.traits.IControlFlow;
import dgir.core.traits.ISingleRegion;
import dgir.core.traits.ISpecificParentOp;
import dgir.core.traits.ITerminator;
import dgir.dialect.arith.ArithOps;
import dgir.dialect.builtin.BuiltinTypes;
import dgir.dialect.cf.CfOps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static dgir.dialect.arith.ArithAttrs.BinModeAttr.BinMode;

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
     * Returns the initial value of the induction variable (operand 0) visible inside the loop body.
     *
     * @return the body value at index 0.
     */
    @Contract(pure = true)
    public @NotNull Value getInitialValue() {
      return getOperand(0).flatMap(Operand::getValue).orElseThrow();
    }

    /**
     * Returns the lower bound value (operand 1) visible inside the loop body.
     *
     * @return the body value at index 1.
     */
    @Contract(pure = true)
    public @NotNull Value getLowerBound() {
      return getOperand(1).flatMap(Operand::getValue).orElseThrow();
    }

    /**
     * Returns the upper bound value (operand 2) visible inside the loop body.
     *
     * @return the body value at index 2.
     */
    @Contract(pure = true)
    public @NotNull Value getUpperBound() {
      return getOperand(2).flatMap(Operand::getValue).orElseThrow();
    }

    /**
     * Returns the step value (operand 3) visible inside the loop body.
     *
     * @return the body value at index 3.
     */
    @Contract(pure = true)
    public @NotNull Value getStep() {
      return getOperand(3).flatMap(Operand::getValue).orElseThrow();
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

    @Override
    public Constructor<? extends ITerminator> getImplicitTerminatorType()
        throws NoSuchMethodException {
      return ContinueOp.class.getConstructor(Location.class);
    }
  }

  final class WhileOp extends ScfOp implements ScfOps, IControlFlow {
    @Override
    public @NotNull String getIdent() {
      return "scf.while";
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return null;
    }

    private WhileOp() {}

    public WhileOp(@NotNull Location location) {
      setOperation(true, Operation.Create(location, this, null, null, null, 2));
    }

    public static void setupStaticForLoop(
        WhileOp op, Value induction, int compareVal, BinMode compMode, int step) {
      Region condRegion = op.getConditionRegion();

      Block continueBlock = condRegion.addBlock(new Block());
      {
        var stepConst = continueBlock.addOperation(new ArithOps.ConstantOp(Location.UNKNOWN, step));
        var addOp =
            continueBlock.addOperation(
                new ArithOps.BinaryOp(
                    Location.UNKNOWN, induction, stepConst.getResult(), BinMode.ADD));
        addOp.setOutputValue(induction);
        continueBlock.addOperation(new ContinueOp(Location.UNKNOWN));
      }

      Block breakBlock = condRegion.addBlock(new Block());
      {
        breakBlock.addOperation(new BreakOp(Location.UNKNOWN));
      }

      Block entryBlock = condRegion.getEntryBlock();
      {
        var compConst =
            entryBlock.addOperation(new ArithOps.ConstantOp(Location.UNKNOWN, compareVal));
        var compResult =
            entryBlock.addOperation(
                new ArithOps.BinaryOp(Location.UNKNOWN, induction, compConst.getValue(), compMode));
        entryBlock.addOperation(
            new CfOps.BranchCondOp(
                Location.UNKNOWN, compResult.getResult(), continueBlock, breakBlock));
      }
    }

    @Contract(pure = true)
    public @NotNull Region getConditionRegion() {
      return getRegion(0).orElseThrow();
    }

    @Contract(pure = true)
    public @NotNull Region getBodyRegion() {
      return getRegion(1).orElseThrow();
    }
  }
}

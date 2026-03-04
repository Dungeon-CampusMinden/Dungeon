package dgir.dialect.cf;

import dgir.core.Dialect;
import dgir.core.Utils;
import dgir.core.debug.Location;
import dgir.core.ir.Block;
import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.core.traits.*;
import dgir.core.traits.IControlFlow;
import dgir.core.traits.INoResult;
import dgir.core.traits.ITerminator;
import dgir.dialect.builtin.BuiltinTypes;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

/**
 * Sealed marker interface for all operations in the {@link CfDialect}.
 *
 * <p>Every concrete op must both extend {@link CfOp} and implement this interface so that {@link
 * Utils.Dialect#allOps} can discover it automatically via reflection.
 */
public sealed interface CfOps {
  /**
   * Abstract base class for all operations in the {@code cf} (control-flow) dialect.
   *
   * <p>Concrete subclasses must implement {@link #getIdent()} and {@link #getVerifier()}, and must
   * implement {@link CfOps} to be enumerated by {@link CfDialect}.
   */
  abstract class CfOp extends Op {

    // =========================================================================
    // Constructors
    // =========================================================================

    /** Default constructor used during dialect registration. */
    CfOp() {
      super();
    }

    // =========================================================================
    // Op Info
    // =========================================================================

    @Contract(pure = true)
    @Override
    public @NotNull Class<? extends Dialect> getDialect() {
      return CfDialect.class;
    }

    @Contract(pure = true)
    @Override
    public @NotNull String getNamespace() {
      return "cf";
    }
  }

  /**
   * Conditional branch that selects between two target blocks based on a boolean condition.
   *
   * <p>This is a terminator: it must be the last operation in its parent block. Control is
   * transferred to {@code target} if the condition is {@code true} ({@code 1}), or to {@code
   * elseTarget} if the condition is {@code false} ({@code 0}).
   *
   * <p>The condition operand must be of type {@link BuiltinTypes.IntegerT#BOOL} ({@code int1}).
   *
   * <p>MLIR reference: {@code cf.br_cond}
   *
   * <pre>{@code
   * cf.br_cond %cond, ^trueBlock, ^falseBlock
   * }</pre>
   */
  final class BranchCondOp extends CfOp implements CfOps, ITerminator, IControlFlow {

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
     * Create a conditional branch.
     *
     * @param location the source location of this operation.
     * @param condition an {@link BuiltinTypes.IntegerT#BOOL} value controlling the branch
     *     direction.
     * @param target the successor block taken when {@code condition} is {@code true}.
     * @param elseTarget the successor block taken when {@code condition} is {@code false}.
     */
    public BranchCondOp(
        @NotNull Location location,
        @NotNull Value condition,
        @NotNull Block target,
        @NotNull Block elseTarget) {
      setOperation(
          Operation.Create(location, this, List.of(condition), List.of(target, elseTarget), null));
      assert condition.getType().equals(BuiltinTypes.IntegerT.BOOL)
          : "Condition must be of type bool/int1.";
    }
  }

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
  final class BranchOp extends CfOp implements CfOps, ITerminator, IControlFlow {

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
     * Create an unconditional branch to {@code target}.
     *
     * @param location the source location of this operation.
     * @param target the successor block to branch to.
     */
    public BranchOp(@NotNull Location location, @NotNull Block target) {
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

  /**
   * Assert that a condition holds at runtime, and abort execution if it does not. This is useful
   * for encoding invariants that cannot be verified statically, but should be checked during
   * testing.
   *
   * <p>It can either be used with a string attribute as its message or with a value as its message
   * or none.
   */
  final class AssertOp extends CfOp implements CfOps, INoResult {
    @Override
    public @NotNull String getIdent() {
      return "cf.assert";
    }

    @Override
    public Function<Operation, Boolean> getVerifier() {
      return operation -> {
        AssertOp assertOp = operation.as(AssertOp.class).orElseThrow();

        if (assertOp.getOperands().isEmpty() || assertOp.getOperands().size() > 2) {
          assertOp.emitError("AssertOp must have either 1 or 2 operands");
          return false;
        }

        if (assertOp.getOperandValue(0).isEmpty()) {
          assertOp.emitError("Condition operand is missing");
          return false;
        }
        if (!assertOp.getOperandValue(0).get().getType().equals(BuiltinTypes.IntegerT.BOOL)) {
          assertOp.emitError("Condition operand must be of type int1");
        }
        if (assertOp.getOperandValue(1).isEmpty()) {
          return true;
        }
        Value messageOperand = assertOp.getOperandValue(1).orElseThrow();
        if (!messageOperand.getType().equals(BuiltinTypes.StringT.INSTANCE)) {
          assertOp.emitError("Message operand must be of type string");
          return false;
        }
        return true;
      };
    }

    private AssertOp() {}

    public AssertOp(@NotNull Location location, @NotNull Value condition) {
      setOperation(Operation.Create(location, this, List.of(condition), null, null));
    }

    public AssertOp(@NotNull Location location, @NotNull Value condition, @NotNull Value message) {
      setOperation(Operation.Create(location, this, List.of(condition, message), null, null));
    }
  }
}

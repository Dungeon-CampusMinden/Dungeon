package dgir.core.traits;

import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import dgir.dialect.func.FuncOps;
import dgir.dialect.scf.ScfOps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Optional;

/**
 * Constrains an operation to be directly nested inside one of a specific set of parent op types.
 *
 * <p>The verifier walks to the immediate parent operation and checks its type against the list
 * returned by {@link #getValidParentTypes()}. An op with no parent passes unconditionally.
 *
 * <p>Examples: {@link ScfOps.BreakOp} (only valid inside {@link ScfOps.ForOp}), {@link
 * ScfOps.ContinueOp} (valid inside {@link ScfOps.IfOp}, {@link ScfOps.ScopeOp}, or {@link
 * ScfOps.ForOp}), {@link FuncOps.ReturnOp} (only valid inside {@link FuncOps.FuncOp}).
 */
public interface ISpecificParentOp extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull ISpecificParentOp ignored) {
    Operation operation = getOperation();
    Optional<Operation> parentOp = operation.getParentOperation();
    if (parentOp.isEmpty()) {
      return true;
    }
    // Check if the parent op has one of the valid types.
    for (Class<? extends Op> validParentType : getValidParentTypes()) {
      if (parentOp.get().isa(validParentType)) {
        return true;
      }
    }
    // If we get here, the parent op is not valid.
    operation.emitError(
        "Operation can only be nested in the following parent operation types: "
            + getValidParentTypes().stream()
                .map(Class::getSimpleName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("")
            + ". Found parent operation of type: "
            + parentOp.get().getDetails().ident());
    return false;
  }

  /**
   * Returns the list of op classes that are allowed as the immediate parent of this operation.
   *
   * @return an unmodifiable list of valid parent op classes.
   */
  @Contract(pure = true)
  @NotNull
  @Unmodifiable
  List<Class<? extends Op>> getValidParentTypes();
}

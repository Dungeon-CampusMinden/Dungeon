package core.traits;

import core.ir.Op;
import core.ir.Operation;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

/**
 * This trait describes an operation that can only have a specific list of parent operation types.
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

  @Contract(pure = true)
  @NotNull
  @Unmodifiable
  List<Class<? extends Op>> getValidParentTypes();
}

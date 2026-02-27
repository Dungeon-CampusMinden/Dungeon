package core.traits;

import core.ir.Type;
import core.ir.Value;
import org.jetbrains.annotations.NotNull;

/**
 * Constrains an operation to have a result value.
 *
 * <p>Convenience accessor {@link #getResult()} delegates to the first result slot.
 */
public interface IHasResult extends IOpTrait {
  default boolean verify(IHasResult ignored) {
    if (getOperation().getOutput().isEmpty()) {
      getOperation().emitError("Operation must have a result.");
      return false;
    }
    if (getOperation().getOutputValue().isEmpty()) {
      getOperation().emitError("Operation must have a result value.");
      return false;
    }
    return true;
  }

  default @NotNull Value getResult() {
    return getOperation()
        .getOutputValue()
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Expected operation to have a result value: " + getOperation()));
  }

  default @NotNull Type getResultType() {
    return getResult().getType();
  }
}

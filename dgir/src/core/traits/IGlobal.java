package core.traits;

import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface IGlobal extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull IGlobal op) {
    return get().getParentOperation()
      // Ensure that the op is inside a global container op
      .map(parent -> parent.hasTrait(IGlobalContainer.class))
      // If the op is not inside another op it can be viewed as a global
      .orElse(true);
  }
}

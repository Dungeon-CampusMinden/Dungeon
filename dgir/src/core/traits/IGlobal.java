package core.traits;

import core.ir.Operation;

import java.util.Objects;

public interface IGlobal extends IOpTrait {
  default boolean verify(IGlobal op) {
    return get().getParentOperation()
      // Ensure that the op is inside a global container op
      .map(parent -> parent.hasTrait(IGlobalContainer.class))
      // If the op is not inside another op it can be viewed as a global
      .orElse(true);
  }
}

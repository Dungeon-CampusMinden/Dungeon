package core.traits;

import java.util.Objects;

public interface IGlobal extends IOpTrait {
  default boolean verify(IGlobal op) {
    // Verify that the operation is contained in a global container if it has a parent.
    if (get().getParentOperation() == null) {
      return true;
    }

    return get().getParentOperation().hasTrait(IGlobalContainer.class);
  }
}

package blockly.vm.dgir.core.traits;

import java.util.Objects;

public interface IGlobal extends IOpTrait {
  default boolean verify(IGlobal op) {
    return Objects.requireNonNull(get().getParentOperation()).hasTrait(IGlobalContainer.class);
  }
}

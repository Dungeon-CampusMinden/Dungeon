package blockly.vm.dgir.core.traits;

import blockly.vm.dgir.core.ir.Region;
import blockly.vm.dgir.core.ir.Value;

public interface ISingleRegion extends IOpTrait {
  default boolean verify(ISingleRegion trait) {
    if (get().getRegions().size() != 1) {
      get().emitError("Operation must have exactly one region.");
      return false;
    }
    return true;
  }

  default Region getRegion() {
    return get().getFirstRegion();
  }

  default Value getArgument(int index) {
    return getRegion().getBodyValue(index);
  }
}

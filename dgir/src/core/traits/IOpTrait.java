package core.traits;

import core.ir.Op;
import core.ir.Operation;

public interface IOpTrait {
  default Operation get() {
    try {
      return ((Op) this).getOperation();
    } catch (ClassCastException e) {
      throw new RuntimeException("Class other than Op implemented IOpTrait: " + getClass().getName(), e);
    }
  }
}

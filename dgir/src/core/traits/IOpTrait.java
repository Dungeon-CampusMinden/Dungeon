package core.traits;

import core.ir.Op;
import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface IOpTrait {
  @Contract(pure = true)
  default @NotNull Operation get() {
    try {
      return ((Op) this).getOperation();
    } catch (ClassCastException e) {
      throw new RuntimeException("Class other than Op implemented IOpTrait: " + getClass().getName(), e);
    }
  }
}

package core.traits;

import core.ir.Operation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface IOpTrait {
  @Contract(pure = true)
  @NotNull Operation getOperation();
}

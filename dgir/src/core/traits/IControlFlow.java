package core.traits;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * This interface marks an operation as having an input on the control flow of the program. Ops that
 * should use this interface include cf.branch, cf.branch_if, etc.
 */
public interface IControlFlow extends IOpTrait {
  @Contract(pure = true)
  default boolean verify(@NotNull IControlFlow ignored) {
    return true;
  }
}

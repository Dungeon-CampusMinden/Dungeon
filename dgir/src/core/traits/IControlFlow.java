package core.traits;

import core.ir.Block;
import core.ir.Operation;

/**
 * This interface marks an operation as having an input on the control flow of the program.
 * Ops that should use this interface include cf.branch, cf.branch_if, etc.
 */
public interface IControlFlow extends IOpTrait {
  default boolean verify(IControlFlow op) {
    return true;
  }
}

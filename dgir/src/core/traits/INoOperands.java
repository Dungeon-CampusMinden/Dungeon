package core.traits;

/**
 * Marks an operation that must have no operands.
 */
public interface INoOperands extends IOpTrait {
  default boolean verify(INoOperands ignored) {
    if (!getOperation().getOperands().isEmpty()) {
      getOperation().emitError("Operation must have no operands.");
      return false;
    }
    return true;
  }
}

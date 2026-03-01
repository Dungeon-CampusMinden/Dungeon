package core.traits;

public interface INoResult extends IOpTrait {
  default boolean verify(INoResult ignored) {
    if (getOperation().getOutput().isPresent()) {
      getOperation().emitError("Operation must not have a result.");
      return false;
    }
    return true;
  }
}

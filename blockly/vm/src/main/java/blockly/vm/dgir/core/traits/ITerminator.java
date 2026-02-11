package blockly.vm.dgir.core.traits;

public interface ITerminator extends IOpTrait {
  default boolean verify(ITerminator trait) {
    // Make sure the terminator is the last operation in the region.
    if (!get().getParent().getOperations().getLast().equals(get()))
    {
      get().emitError("Terminator must be the last operation in the region.");
      return false;
    }
    return true;
  }
}

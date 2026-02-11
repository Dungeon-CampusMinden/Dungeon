package blockly.vm.dgir.core.traits;

/**
 * Marks an operation as being isolated from above. This means that values defined in parent operations do not spill
 * into this operation. This is used for operations that are meant to be self-contained, such as function definitions,
 * and isolates them from the surrounding context.
 */
public interface IIsolatedFromAbove extends IOpTrait {
  default boolean verify(IIsolatedFromAbove op) {
    // The verification of this trait is done in the reaching definitions analysis.
    return true;
  }
}

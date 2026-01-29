package blockly.vm.dgir.core.opinterfaces;

import blockly.vm.dgir.core.Block;
import blockly.vm.dgir.core.Op;

/**
 * This interface marks an operation as having an input on the control flow of the program.
 * Ops that should use this interface include func.return, cf.branch
 */
public interface IControlFlowOp<
  // Make sure only Ops can implement this interface and pass the derived type to the base OpTrait
  DerivedT extends Op & IOpTrait<DerivedT>> extends IOpTrait<DerivedT> {
  Block getSuccessor();
}

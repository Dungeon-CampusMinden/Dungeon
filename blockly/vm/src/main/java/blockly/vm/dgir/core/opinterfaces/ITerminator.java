package blockly.vm.dgir.core.opinterfaces;

import blockly.vm.dgir.core.Op;

public interface ITerminator<
  DerivedT extends Op & IOpTrait<DerivedT>>
  extends IOpTrait<DerivedT> {
}

package blockly.vm.dgir.dialect.arith;

import blockly.vm.api.VM;
import blockly.vm.dgir.core.Block;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.ConstantValue;

public class ConstantOp extends Operation {
  private ConstantValue value;

  public ConstantOp() {
    super(Arith.class);
  }

  @Override
  public boolean fromString(CharSequence json, Block containingBlock) {
    return false;
  }

  @Override
  public void run(VM.State state) {

  }

  public ConstantValue getValue() {
    return value;
  }

  public void setValue(ConstantValue value) {
    this.value = value;
  }
}

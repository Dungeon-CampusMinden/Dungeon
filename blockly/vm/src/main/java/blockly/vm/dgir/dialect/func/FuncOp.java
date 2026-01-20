package blockly.vm.dgir.dialect.func;

import blockly.vm.api.VM;
import blockly.vm.dgir.core.Block;
import blockly.vm.dgir.core.IDialect;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Region;

public class FuncOp extends Operation {
  private final Region region = Region.CreateWithBlock(this);

  public FuncOp() {
    super(Func.class);
  }

  @Override
  public boolean fromString(CharSequence json, Block containingBlock) {
    return false;
  }

  @Override
  public void run(VM.State state) {

  }
}

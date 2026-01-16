package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.Block;
import blockly.vm.dgir.core.IOperation;
import blockly.vm.api.VM;
import tools.jackson.databind.JsonNode;

public class ProgramOp extends IOperation {
  public ProgramOp() {
    super("builtin", "program");
  }

  @Override
  public boolean fromJson(JsonNode json, Block containingBlock) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean fromString(CharSequence json, Block containingBlock) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void run(VM.State state) {

  }
}

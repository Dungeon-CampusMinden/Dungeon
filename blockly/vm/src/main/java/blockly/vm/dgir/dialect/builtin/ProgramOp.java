package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Region;

public class ProgramOp extends Operation {
  public ProgramOp() {
     addRegion(Region.createWithBlock());
  }

  @Override
  public String getIdent() {
    return "program";
  }

  @Override
  public String getNamespace() {
    return "";
  }
}

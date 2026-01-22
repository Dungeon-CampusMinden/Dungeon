package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Region;

public class FuncOp extends Operation {
  private String name;

  FuncOp() {
  }

  public FuncOp(String name, boolean withDefaultRegion) {
    setName(name);
    if (withDefaultRegion)
      addRegion(Region.createWithBlock());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getIdent() {
    return "func.func";
  }

  @Override
  public String getNamespace() {
    return "func";
  }
}

package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Region;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FuncOp extends Operation {
  private String name;

  public FuncOp() {
    this.name = "";
    addRegion(Region.createWithBlock());
  }

  @JsonCreator
  public FuncOp(@JsonProperty("name") String name) {
    this.name = name;
    if (!hasRegion()){
      addRegion(Region.createWithBlock());
    }
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

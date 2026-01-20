package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Region;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FuncOp extends Operation {
  public String ident;
  public Region region;

  public FuncOp() {
    super(Func.class, "func");
    region = Region.createWithBlock();
  }

  @JsonCreator
  public FuncOp(@JsonProperty("ident") String ident, @JsonProperty("region") Region region) {
    super(Func.class, "func");
    this.ident = ident;
    this.region = region;
  }
}

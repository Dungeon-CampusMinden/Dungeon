package blockly.vm.dgir.dialect.func;

import blockly.vm.dgir.core.IRegionContainingOp;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Region;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class FuncOp extends Operation implements IRegionContainingOp {
  private final String ident;
  private final Region region;

  public FuncOp() {
    super(Func.class, "func");
    this.ident = "func_" + UUID.randomUUID();
    region = Region.createWithBlock();
  }

  public FuncOp(String ident) {
    super(Func.class, "func");
    this.ident = ident;
    region = Region.createWithBlock();
  }

  @JsonCreator
  public FuncOp(@JsonProperty("ident") String ident, @JsonProperty("region") Region region) {
    super(Func.class, "func");
    this.ident = ident;
    this.region = region;
  }

  public String getIdent() {
    return ident;
  }

  @Override
  public Operation getOperation() {
    return this;
  }

  @Override
  public Region getRegion() {
    return region;
  }

}

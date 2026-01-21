package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.IRegionContainingOp;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Region;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProgramOp extends Operation implements IRegionContainingOp {
  private final Region region;

  public ProgramOp() {
    super(Builtin.class, "program");
    region = Region.createWithBlock();
  }

  @JsonCreator
  public ProgramOp(@JsonProperty("region") Region region) {
    super(Builtin.class, "program");
    this.region = region;
  }

  @Override
  public Region getRegion() {
    return region;
  }

  @Override
  public Operation getOperation() {
    return this;
  }
}

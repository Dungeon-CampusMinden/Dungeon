package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IRegionContainingOp {
  public Region getRegion();
  @JsonIgnore
  public Operation getOperation();
}

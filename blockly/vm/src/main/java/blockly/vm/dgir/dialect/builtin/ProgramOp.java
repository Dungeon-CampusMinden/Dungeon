package blockly.vm.dgir.dialect.builtin;

import blockly.vm.dgir.core.Block;
import blockly.vm.dgir.core.IDialect;
import blockly.vm.dgir.core.Operation;
import blockly.vm.api.VM;
import blockly.vm.dgir.core.Region;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
  generator = ObjectIdGenerators.PropertyGenerator.class,
  property = "region")
public class ProgramOp extends Operation {

  @JsonManagedReference
  private final Region region = Region.CreateWithBlock(this);

  @JsonCreator
  public ProgramOp() {
    super(Builtin.class);
  }

  @Override
  public boolean fromString(CharSequence json, Block containingBlock) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void run(VM.State state) {

  }

  public Region getRegion() {
    return region;
  }
}

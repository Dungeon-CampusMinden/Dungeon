package blockly.vm.dgir.dialect.arith;

import blockly.vm.dgir.core.NamedAttribute;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Value;
import com.fasterxml.jackson.annotation.JsonCreator;

public class ConstantOp extends Operation {
  @JsonCreator
  public ConstantOp() {
    super(Arith.class, "const");
  }

  public ConstantOp(NamedAttribute value) {
    this();
    getAttributes().add(value);
  }

  public NamedAttribute getValue() {
    return getAttributes().getFirst();
  }

  @Override
  public Value getOutput() {
    if (super.getOutput() == null)
    {
      setOutput(new Value("%x", getValue().getAttribute().getType()));
    }
    return super.getOutput();
  }
}

package blockly.vm.dgir.dialect.arith;

import blockly.vm.dgir.core.DynamicValue;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.ConstantValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConstantOp extends Operation {
  private ConstantValue value;

  public ConstantOp() {
    super(Arith.class, "const");
  }

  @JsonCreator
  public ConstantOp(@JsonProperty("value") ConstantValue value) {
    super(Arith.class, "const");
    setValue(value);
  }

  public ConstantValue getValue() {
    return value;
  }

  public void setValue(ConstantValue value) {
    this.value = value;
    // Only update the output if it isn't set yet or doesn't fit the type of the constant value
    // This is mostly important during deserialization
    if (getOutput() == null || getOutput().getType() == null || getOutput().getIdent().isEmpty())
      setOutput(new DynamicValue("%const", value));
  }
}

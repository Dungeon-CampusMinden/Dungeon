package blockly.vm.dgir.dialect.arith;

import blockly.vm.dgir.core.DynamicValue;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.ConstantValue;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ConstantOp extends Operation {
  public ConstantValue value;

  public ConstantOp() {
    super(Arith.class, "const");
  }

  @JsonIgnore
  public void setValue(ConstantValue value) {
    this.value = value;
    var outputValue = new DynamicValue();
    outputValue.setIdentUnique("%const");
    outputValue.type = value.type;
    output = outputValue;
  }
}

package blockly.vm.dgir.dialect.io;

import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Value;
import blockly.vm.dgir.core.ValueOperand;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;


public class PrintOp extends Operation {
  @JsonCreator
  public PrintOp() {
    super(IO.class, "print");
  }

  public PrintOp(List<Value> operands) {
    this();
    for (Value operand : operands) {
      getOperands().add(new ValueOperand(this, operand));
    }
  }
}

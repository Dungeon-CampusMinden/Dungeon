package blockly.vm.dgir.dialect.io;

import blockly.vm.dgir.core.IInputValue;
import blockly.vm.dgir.core.Operation;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class PrintOp extends Operation {
  private final List<IInputValue> inputs;

  public PrintOp() {
    super(IO.class, "print");
    inputs = new ArrayList<>();
  }

  @JsonCreator
  public PrintOp(@JsonProperty("inputs") List<IInputValue> inputs) {
    super(IO.class, "print");
    this.inputs = inputs;
  }

  public List<IInputValue> getInputs() {
    return inputs;
  }
}

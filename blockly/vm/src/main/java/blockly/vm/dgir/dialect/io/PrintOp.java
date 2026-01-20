package blockly.vm.dgir.dialect.io;

import blockly.vm.api.VM;
import blockly.vm.dgir.core.Block;
import blockly.vm.dgir.core.IInputValue;
import blockly.vm.dgir.core.IValue;
import blockly.vm.dgir.core.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrintOp extends Operation {
  private final List<IInputValue> arguments = new ArrayList<>();

  public PrintOp() {
    super(IO.class);
  }

  public PrintOp(List<IInputValue> arguments) {
    this();
    this.arguments.addAll(arguments);
  }

  public List<IInputValue> getArguments() {
    return arguments;
  }

  @Override
  public boolean fromString(CharSequence json, Block containingBlock) {
    return false;
  }

  @Override
  public void run(VM.State state) {
    System.out.println(arguments.stream().map(IValue::getValue).map(Object::toString).reduce("", (a, b) -> a + b));
  }
}

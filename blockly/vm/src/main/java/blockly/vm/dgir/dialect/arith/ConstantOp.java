package blockly.vm.dgir.dialect.arith;

import blockly.vm.dgir.core.NamedAttribute;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.OperationResult;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ConstantOp extends Operation {
  public ConstantOp(){}

  public ConstantOp(NamedAttribute value) {
    addAttribute(value);
    setOutput(new OperationResult(value.getAttribute().getType(), this));
  }

  @JsonIgnore
  public NamedAttribute getValue() {
    return getOrCreateAttributes().getFirst();
  }

  @Override
  public String getIdent() {
    return "artih.constant";
  }

  @Override
  public String getNamespace() {
    return "arith";
  }
}

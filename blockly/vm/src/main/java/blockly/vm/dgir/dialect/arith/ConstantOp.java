package blockly.vm.dgir.dialect.arith;

import blockly.vm.dgir.core.Attribute;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.dialect.builtin.attributes.IntegerAttribute;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ConstantOp extends Operation {
  public ConstantOp() {
  }

  @JsonIgnore
  public Attribute getValue() {
    return getOrCreateAttribute("value", new IntegerAttribute(0)).getAttribute();
  }

  public void setValue(Attribute attribute) {
    getOrCreateAttribute("value", attribute).setAttribute(attribute);
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

package blockly.vm.dgir.dialect.builtin.attributes;

import blockly.vm.dgir.core.Attribute;
import blockly.vm.dgir.dialect.builtin.types.StringT;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StringAttribute extends Attribute {
  private String value;

  public StringAttribute() {
    super(StringT.INSTANCE);
  }

  @JsonCreator
  public StringAttribute(@JsonProperty("value") String value) {
    this();
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String getIdent() {
    return "stringAttr";
  }

  @Override
  public String getNamespace() {
    return "";
  }
}

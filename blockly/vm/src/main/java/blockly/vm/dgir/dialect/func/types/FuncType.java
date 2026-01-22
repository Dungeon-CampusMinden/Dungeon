package blockly.vm.dgir.dialect.func.types;

import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.dialect.func.Func;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FuncType extends Type {
  public static final FuncType INSTANCE = new FuncType();

  private final List<Type> inputs;
  private final Type output;

  public FuncType() {
    inputs = null;
    output = null;
  }

  @JsonCreator
  public FuncType(@JsonProperty("inputs") List<Type> inputs, @JsonProperty("output") Type output) {
    this.inputs = inputs;
    this.output = output;
  }

  public List<Type> getInputs() {
    return inputs;
  }

  public Type getOutput() {
    return output;
  }

  @Override
  public boolean validate(Object value) {
    return false;
  }

  @Override
  public String getIdent() {
    return "func.func";
  }

  @Override
  public String getNamespace() {
    return "func";
  }
}

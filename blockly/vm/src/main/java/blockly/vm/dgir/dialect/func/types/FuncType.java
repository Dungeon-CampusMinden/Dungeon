package blockly.vm.dgir.dialect.func.types;

import blockly.vm.dgir.core.type.Type;
import blockly.vm.dgir.dialect.func.Func;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FuncType extends Type {
  public static final FuncType INSTANCE = new FuncType();

  private final List<Type> inputs;
  private final Type output;

  public FuncType() {
    super(Func.class, "func");
    inputs = null;
    output = null;
  }

  @JsonCreator
  public FuncType(@JsonProperty("inputs") List<Type> inputs, @JsonProperty("output") Type output) {
    super(Func.class, "func");
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
}

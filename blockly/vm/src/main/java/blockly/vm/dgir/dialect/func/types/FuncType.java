package blockly.vm.dgir.dialect.func.types;

import blockly.vm.dgir.core.type.Type;
import blockly.vm.dgir.dialect.func.Func;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FuncType extends Type {
  private final List<Type> arguments;
  private final Type returnType;

  public FuncType() {
    super(Func.class, "func");
    arguments = null;
    returnType = null;
  }



  @Override
  public boolean validate(Object value) {
    return false;
  }
}

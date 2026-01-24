package blockly.vm.dgir.dialect.func.types;

import blockly.vm.dgir.core.Dialect;
import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.core.TypeName;
import blockly.vm.dgir.dialect.func.Func;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FuncType extends Type {
  public static final FuncType INSTANCE = new FuncType();

  private List<Type> inputs;
  private Type output;

  public FuncType() {
    inputs = null;
    output = null;
  }

  @JsonCreator
  public FuncType(@JsonProperty("inputs") List<Type> inputs, @JsonProperty("output") Type output) {
    this.inputs = inputs;
    this.output = output;
  }

  @Override
  public TypeName.Impl createImpl() {
    class FuncTypeModel extends TypeName.Impl {
      public FuncTypeModel(String name, Class<? extends Type> type, Dialect dialect) {
        super(name, type, dialect);
      }
    }
    return new FuncTypeModel(getIdent(), getClass(), Dialect.get(Func.class));
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

  public static String getIdent() {
    return "func.func";
  }

  public static String getNamespace() {
    return "func";
  }
}

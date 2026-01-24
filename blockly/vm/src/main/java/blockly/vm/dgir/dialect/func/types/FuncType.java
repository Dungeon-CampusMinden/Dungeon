package blockly.vm.dgir.dialect.func.types;

import blockly.vm.dgir.core.*;
import blockly.vm.dgir.dialect.builtin.types.StringT;
import blockly.vm.dgir.dialect.func.Func;

import java.util.ArrayList;
import java.util.List;

public class FuncType extends Type {
  public static final FuncType INSTANCE = new FuncType();

  private List<Type> inputs = new ArrayList<>();
  private Type output = null;

  @Override
  public TypeDetails.Impl createImpl() {
    class FuncTypeModel extends TypeDetails.Impl {
      public FuncTypeModel(String name, Class<? extends Type> type, Dialect dialect) {
        super(name, type, dialect);
      }
    }
    return new FuncTypeModel(getIdent(), getClass(), Dialect.get(Func.class));
  }

  public FuncType() {
    super(RegisteredTypeDetails.lookup(getIdent()).orElse(null));
  }

  public FuncType(List<Type> inputs, Type output) {
    super(RegisteredTypeDetails.lookup(getIdent()).orElseThrow());
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

  public static String getIdent() {
    return "func.func";
  }

  public static String getNamespace() {
    return "func";
  }
}

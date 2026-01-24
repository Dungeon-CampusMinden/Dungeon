package blockly.vm.dgir.dialect.builtin.types;

import blockly.vm.dgir.core.Dialect;
import blockly.vm.dgir.core.PrimitiveType;
import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.core.TypeName;
import blockly.vm.dgir.dialect.builtin.Builtin;

public class StringT extends PrimitiveType {
  public static final StringT INSTANCE = new StringT();

  public StringT() {

  }

  @Override
  public TypeName.Impl createImpl() {
    class StringTModel extends TypeName.Impl {
      public StringTModel() {
        super(getIdent(), StringT.class, Dialect.get(Builtin.class));
      }
    }
    return new StringTModel();
  }

  @Override
  public boolean validate(Object value) {
    return value instanceof String;
  }

  public static String getIdent() {
    return "string";
  }

  public static String getNamespace() {
    return "";
  }
}

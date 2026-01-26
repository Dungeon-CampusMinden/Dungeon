package blockly.vm.dgir.dialect.builtin.types;

import blockly.vm.dgir.core.Dialect;
import blockly.vm.dgir.core.RegisteredTypeDetails;
import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.core.TypeDetails;
import blockly.vm.dgir.dialect.builtin.Builtin;

public class StringT extends Type {
  public static final StringT INSTANCE = new StringT();

  public StringT() {
  }

  @Override
  public TypeDetails.Impl createImpl() {
    class StringTModel extends TypeDetails.Impl {
      public StringTModel() {
        super(StringT.getIdent(), StringT.class, Dialect.get(Builtin.class));
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

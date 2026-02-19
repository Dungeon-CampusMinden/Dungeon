package dialect.builtin.types;

import core.Dialect;
import core.ir.Type;
import core.detail.TypeDetails;
import dialect.builtin.Builtin;

public class StringT extends Type {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final StringT INSTANCE = new StringT();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public TypeDetails.Impl createImpl() {
    class StringTModel extends TypeDetails.Impl {
      StringTModel() {
        super(INSTANCE, StringT.getIdent(), StringT.class, Dialect.get(Builtin.class));
      }
    }
    return new StringTModel();
  }

  public static String getIdent() {
    return "string";
  }

  public static String getNamespace() {
    return "";
  }

  // =========================================================================
  // Constructors
  // =========================================================================

  public StringT() {
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Override
  public boolean validate(Object value) {
    return value instanceof String;
  }
}

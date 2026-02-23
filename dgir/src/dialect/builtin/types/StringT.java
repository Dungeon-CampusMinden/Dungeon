package dialect.builtin.types;

import core.Dialect;
import core.detail.TypeDetails;
import core.ir.Type;
import dialect.builtin.BuiltinDialect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringT extends Type {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final StringT INSTANCE = new StringT();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public TypeDetails.@NotNull Impl createImpl() {
    class StringTModel extends TypeDetails.Impl {
      StringTModel() {
        super(INSTANCE, StringT.getIdent(), StringT.class, Dialect.getOrThrow(BuiltinDialect.class));
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

  public StringT() {}

  // =========================================================================
  // Functions
  // =========================================================================

  @Override
  public boolean validate(@Nullable Object value) {
    return value instanceof String;
  }
}

package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.TypedAttribute;
import dialect.builtin.BuiltinDialect;
import dialect.builtin.types.StringT;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringAttribute extends TypedAttribute {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final StringAttribute INSTANCE = new StringAttribute();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public AttributeDetails.@NotNull Impl createImpl() {
    class StringAttributeModel extends AttributeDetails.Impl {
      StringAttributeModel() {
        super(StringAttribute.getIdent(), StringAttribute.class, Dialect.getOrThrow(BuiltinDialect.class));
      }
    }
    return new StringAttributeModel();
  }

  public static String getIdent() {
    return "stringAttr";
  }

  public static String getNamespace() {
    return "";
  }

  // =========================================================================
  // Members
  // =========================================================================

  private String value;

  // =========================================================================
  // Constructors
  // =========================================================================

  public StringAttribute() {
    super(StringT.INSTANCE);
  }

  public StringAttribute(String value) {
    super(StringT.INSTANCE);
    this.value = value;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @Nullable Object getStorage() {
    return value;
  }

  @Contract(pure = true)
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

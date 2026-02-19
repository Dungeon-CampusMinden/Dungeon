package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.TypedAttribute;
import dialect.builtin.Builtin;
import dialect.builtin.types.StringT;

public class StringAttribute extends TypedAttribute {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final StringAttribute INSTANCE = new StringAttribute();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public AttributeDetails.Impl createImpl() {
    class StringAttributeModel extends AttributeDetails.Impl {
      StringAttributeModel() {
        super(StringAttribute.getIdent(), StringAttribute.class, Dialect.get(Builtin.class));
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

  @Override
  public String getStorage() {
    return value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

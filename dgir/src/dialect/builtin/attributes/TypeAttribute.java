package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.Attribute;
import core.ir.Type;
import dialect.builtin.Builtin;

public class TypeAttribute extends Attribute {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final TypeAttribute INSTANCE = new TypeAttribute();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public AttributeDetails.Impl createImpl() {
    class TypeAttributeModel extends AttributeDetails.Impl {
      TypeAttributeModel() {
        super(TypeAttribute.getIdent(), TypeAttribute.class, Dialect.get(Builtin.class));
      }
    }
    return new TypeAttributeModel();
  }

  public static String getIdent() {
    return "typeAttr";
  }

  public static String getNamespace() {
    return "";
  }

  // =========================================================================
  // Members
  // =========================================================================

  private Type type;

  // =========================================================================
  // Constructors
  // =========================================================================

  public TypeAttribute() {
  }

  public TypeAttribute(Type type) {
    this.type = type;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Override
  public Type getStorage() {
    return type;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
}

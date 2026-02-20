package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.Attribute;
import dialect.builtin.Builtin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SymbolRefAttribute extends Attribute {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final SymbolRefAttribute INSTANCE = new SymbolRefAttribute();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public AttributeDetails.@NotNull Impl createImpl() {
    class SymbolRefAttributeModel extends AttributeDetails.Impl {
      SymbolRefAttributeModel() {
        super(SymbolRefAttribute.getIdent(), SymbolRefAttribute.class, Dialect.getOrThrow(Builtin.class));
      }
    }
    return new SymbolRefAttributeModel();
  }

  public static String getIdent() {
    return "symbolRefAttr";
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

  public SymbolRefAttribute() {
  }

  public SymbolRefAttribute(String value) {
    this.value = value;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Override
  public @Nullable String getStorage() {
    return value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}

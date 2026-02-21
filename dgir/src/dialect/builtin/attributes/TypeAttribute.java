package dialect.builtin.attributes;

import core.*;
import core.detail.AttributeDetails;
import core.ir.Attribute;
import core.ir.Type;
import dialect.builtin.Builtin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TypeAttribute extends Attribute {

  // =========================================================================
  // Static Fields
  // =========================================================================

  public static final @NotNull TypeAttribute INSTANCE = new TypeAttribute();

  // =========================================================================
  // Type Info
  // =========================================================================

  @Override
  public @NotNull AttributeDetails.Impl createImpl() {
    class TypeAttributeModel extends AttributeDetails.Impl {
      TypeAttributeModel() {
        super(TypeAttribute.getIdent(), TypeAttribute.class, Dialect.getOrThrow(Builtin.class));
      }
    }
    return new TypeAttributeModel();
  }

  public static @NotNull String getIdent() {
    return "typeAttr";
  }

  public static @NotNull String getNamespace() {
    return "";
  }

  // =========================================================================
  // Members
  // =========================================================================

  private @Nullable Type type;

  // =========================================================================
  // Constructors
  // =========================================================================

  public TypeAttribute() {}

  public TypeAttribute(@Nullable Type type) {
    this.type = type;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  @Override
  public @Nullable Type getStorage() {
    return type;
  }

  @Contract(pure = true)
  public @NotNull Optional<Type> getType() {
    return Optional.ofNullable(type);
  }

  public void setType(@Nullable Type type) {
    this.type = type;
  }
}

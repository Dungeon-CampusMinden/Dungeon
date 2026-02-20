package core.ir;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import core.Utils;
import core.detail.RegisteredTypeDetails;
import core.detail.TypeDetails;
import core.serialization.TypeDeserializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;

// We have to use the deserializer because we cant use @JsonCreator on static methods and therefore
// can put the logic
// directly in this class.
@JsonDeserialize(using = TypeDeserializer.class)
public abstract class Type {

  // =========================================================================
  // Members
  // =========================================================================

  @JsonIgnore private @NotNull TypeDetails details;

  // =========================================================================
  // Type Info
  // =========================================================================

  /** Create and return the impl object that describes this type kind. */
  public abstract @NotNull TypeDetails.Impl createImpl();

  // =========================================================================
  // Constructors
  // =========================================================================

  public Type() {
    details = TypeDetails.get(getClass());
  }

  public Type(@NotNull TypeDetails typeDetails) {
    details = typeDetails;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  public @NotNull TypeDetails getDetails() {
    return details;
  }

  public void setDetails(@NotNull TypeDetails details) {
    assert Utils.Caller.getCallingClass().isAssignableFrom(RegisteredTypeDetails.class)
        : "Only RegisteredTypeDetails is allowed to set details. Was called from "
            + Utils.Caller.getCallingClass().getName();
    this.details = details;
  }

  /** Return this type's parameterized ident string (used as the JSON serialized form). */
  @Contract(pure = true)
  @JsonValue
  public @NotNull String getParameterizedIdent() {
    return details.getParameterizedIdent(this);
  }

  /** Validate whether {@code value} is a legal storage value for this type. */
  @Contract(pure = true)
  public abstract boolean validate(@Nullable Object value);

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public String toString() {
    return getParameterizedIdent();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return (obj instanceof Type other)
        && this.getParameterizedIdent().equals(other.getParameterizedIdent());
  }

  @Override
  public int hashCode() {
    return getParameterizedIdent().hashCode();
  }
}

package core.ir;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import core.Dialect;
import core.detail.AttributeDetails;
import core.serialization.AttributeTypeIdResolver;
import java.io.Serializable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.CUSTOM,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "ident")
@JsonTypeIdResolver(AttributeTypeIdResolver.class)
@JsonPropertyOrder({"ident", "type"})
public abstract class Attribute implements Serializable {

  // =========================================================================
  // Members
  // =========================================================================

  private @NotNull AttributeDetails details;

  // =========================================================================
  // Attribute Info
  // =========================================================================

  /**
   * Returns the unique ident string for this attribute kind (e.g. {@code "integerAttr"}).
   *
   * @return the ident string, never {@code null}.
   */
  @Contract(pure = true)
  public abstract @NotNull String getIdent();

  /**
   * Returns the namespace prefix for this attribute kind (e.g. {@code ""} for builtin attributes).
   *
   * @return the namespace string, never {@code null}.
   */
  @Contract(pure = true)
  public abstract @NotNull String getNamespace();

  /**
   * Returns the class of the dialect that contributes this attribute kind.
   *
   * @return the dialect class, never {@code null}.
   */
  @Contract(pure = true)
  public abstract @NotNull Class<? extends Dialect> getDialect();

  // =========================================================================
  // Constructors
  // =========================================================================

  public Attribute() {
    this.details = AttributeDetails.get(getClass());
  }

  public Attribute(@NotNull AttributeDetails details) {
    this.details = details;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  @JsonIgnore
  public @NotNull AttributeDetails getDetails() {
    return details;
  }

  /** Package-private — only {@link AttributeDetails.Registered#insert} may call this. */
  public void setDetails(@NotNull AttributeDetails details) {
    this.details = details;
  }

  @JsonProperty("ident")
  private @NotNull String getIdentForJson() {
    return details.ident();
  }

  /** Return the raw storage value of this attribute (used for serialization and display). */
  @Contract(pure = true)
  @JsonIgnore
  public abstract @org.jetbrains.annotations.Nullable Object getStorage();
}

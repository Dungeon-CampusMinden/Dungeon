package core.ir;

import core.Utils;
import core.detail.AttributeDetails;
import core.detail.RegisteredAttributeDetails;
import core.serialization.AttributeTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.annotation.JsonTypeIdResolver;

import java.io.Serializable;

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

  private AttributeDetails details;

  // =========================================================================
  // Attribute Info
  // =========================================================================

  /** Create and return the impl object that describes this attribute kind. */
  public abstract AttributeDetails.Impl createImpl();

  // =========================================================================
  // Constructors
  // =========================================================================

  public Attribute() {
    setDetails(AttributeDetails.get(getClass()));
  }

  public Attribute(AttributeDetails details) {
    setDetails(details);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @JsonIgnore
  public AttributeDetails getDetails() {
    return details;
  }

  public void setDetails(AttributeDetails details) {
    // Only subclasses of Attribute and RegisteredAttributeDetails may set the details
    assert Utils.Caller.getCallingClass().isAssignableFrom(Attribute.class)
      || Utils.Caller.getCallingClass().isAssignableFrom(RegisteredAttributeDetails.class)
      : "Only subclasses of Attribute can set the details. Was called from " + Utils.Caller.getCallingClass().getName();
    this.details = details;
  }

  @JsonProperty("ident")
  private String getIdent() {
    return details.getIdent();
  }

  /** Return the raw storage value of this attribute (used for serialization and display). */
  @JsonIgnore
  public abstract Object getStorage();
}

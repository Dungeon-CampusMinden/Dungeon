package blockly.vm.dgir.core.ir;

import blockly.vm.dgir.core.Utils;
import blockly.vm.dgir.core.detail.AttributeDetails;
import blockly.vm.dgir.core.detail.RegisteredAttributeDetails;
import blockly.vm.dgir.core.serialization.AttributeTypeIdResolver;
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
  private AttributeDetails details;

  public abstract AttributeDetails.Impl createImpl();

  public Attribute() {
    setDetails(AttributeDetails.get(getClass()));
  }

  public Attribute(AttributeDetails details) {
    setDetails(details);
  }

  @JsonIgnore
  public AttributeDetails getDetails() {
    return details;
  }

  public void setDetails(AttributeDetails details) {
    // Make sure that only classes extending Attribute call this method.
    assert Utils.Caller.getCallingClass().isAssignableFrom(Attribute.class)
      || Utils.Caller.getCallingClass().isAssignableFrom(RegisteredAttributeDetails.class)
      : "Only subclasses of Attribute can set the details. Was called from " + Utils.Caller.getCallingClass().getName();

    this.details = details;
  }

  @JsonProperty("ident")
  private String getIdent() {
    return details.getIdent();
  }

  @JsonIgnore
  public abstract Object getStorage();
}

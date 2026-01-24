package blockly.vm.dgir.core;

import java.io.Serializable;

public abstract class Attribute implements Serializable {
  private AttributeDetails details;

  public abstract AttributeDetails.Impl createImpl();

  public Attribute() {
    setDetails(AttributeDetails.get(getClass()));
  }

  public Attribute(AttributeDetails details) {
    setDetails(details);
  }

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

  public abstract Type getType();
}

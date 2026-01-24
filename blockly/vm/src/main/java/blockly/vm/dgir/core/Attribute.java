package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

public abstract class Attribute implements Serializable {
  private AttributeName name;

  public abstract AttributeName.Impl createImpl();

  public Attribute() {
  }

  public Attribute(AttributeName name) {
    this.name = name;
  }

  public AttributeName getName() {
    return name;
  }

  public void setName(AttributeName name) {
    // Make sure that only classes extending Attribute call this method.
    assert Utils.Caller.getCallingClass().isAssignableFrom(Attribute.class) : "Only subclasses of Attribute can set the name. Was called from " + Utils.Caller.getCallingClass().getName();
    assert this.name == null : "Attribute name already set.";
    assert name != null : "Attribute name cannot be null.";

    this.name = name;
  }

  public abstract Type getType();
}

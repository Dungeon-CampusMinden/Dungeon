package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonPropertyOrder({"ident", "type", "value"})
public abstract class Attribute implements IIdentifiableType, Serializable {
  private final Type type;

  public Attribute() {
    this.type = null;
  }

  @JsonCreator
  public Attribute(@JsonProperty("type") Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}

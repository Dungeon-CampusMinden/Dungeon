package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class NamedAttribute {
  private final String name;
  private Attribute attribute;
  @JsonBackReference
  public Operation owner;

  @JsonCreator
  public NamedAttribute(@JsonProperty("name") String name, @JsonProperty("attribute") Attribute attribute) {
    this.name = name;
    this.attribute = attribute;
  }

  public String getName() {
    return name;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public void setAttribute(Attribute attribute) {
    this.attribute = attribute;
  }
}

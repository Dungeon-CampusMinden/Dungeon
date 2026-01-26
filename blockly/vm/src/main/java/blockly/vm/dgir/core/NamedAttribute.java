package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.NamedAttributeSerializer;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = NamedAttributeSerializer.class)
public final class NamedAttribute {
  private final String name;
  private Attribute attribute;

  @JsonBackReference
  private Operation parent;

  public NamedAttribute(String name, Attribute attribute) {
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

  public Operation getParent() {
    return parent;
  }

  public void setParent(Operation operation) {
    assert Utils.Caller.getCallingClass() == Operation.class : "Assigning the parent of a named attribute is only allowed from the Operation class. Was called from " + Utils.Caller.getCallingClass().getName();
    assert this.parent == null || this.parent == operation : "Named attribute already assigned to another operation.";

    this.parent = operation;
  }
}

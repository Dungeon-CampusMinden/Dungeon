package blockly.vm.dgir.core;

public final class NamedAttribute {
  private final String name;
  private Attribute attribute;
  public Operation parent;

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
    this.parent = operation;
  }
}

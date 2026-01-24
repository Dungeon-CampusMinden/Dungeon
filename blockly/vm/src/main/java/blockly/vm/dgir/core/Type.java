package blockly.vm.dgir.core;

public abstract class Type {
  private TypeName name;

  // Every type should be default constructible.
  public Type() {
  }

  public Type(TypeName typeName) {
    setName(typeName);
  }

  public abstract TypeName.Impl createImpl();

  public TypeName getName() {
    return name;
  }

  protected void setName(TypeName name) {
    assert this.name == null || this.name == name : "Type name already set.";
    assert name != null : "Type name cannot be null.";

    this.name = name;
  }

  public abstract boolean validate(Object value);
}

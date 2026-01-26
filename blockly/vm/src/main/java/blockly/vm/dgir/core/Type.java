package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.TypeSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = TypeSerializer.class)
public abstract class Type {
  private TypeDetails details;

  // Every type should be default constructible.
  public Type() {
    setDetails(TypeDetails.get(getClass()));
  }

  public Type(TypeDetails typeDetails) {
    setDetails(typeDetails);
  }

  public abstract TypeDetails.Impl createImpl();

  public TypeDetails getDetails() {
    return details;
  }

  protected void setDetails(TypeDetails details) {
    assert Utils.Caller.getCallingClass().isAssignableFrom(Type.class)
      || Utils.Caller.getCallingClass().isAssignableFrom(RegisteredTypeDetails.class)
      : "Only subclasses of Type can set the details. Was called from " + Utils.Caller.getCallingClass().getName();

    this.details = details;
  }

  public abstract boolean validate(Object value);
}

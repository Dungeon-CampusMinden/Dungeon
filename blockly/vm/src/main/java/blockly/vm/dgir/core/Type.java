package blockly.vm.dgir.core;

import blockly.vm.dgir.core.serialization.TypeSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = TypeSerializer.class)
public abstract class Type {
  private TypeDetails details;

  public Type() {
    details = TypeDetails.get(getClass());
  }

  public Type(TypeDetails typeDetails) {
    details = typeDetails;
  }

  public abstract TypeDetails.Impl createImpl();

  public TypeDetails getDetails() {
    return details;
  }

  void setDetails(TypeDetails details) {
    assert Utils.Caller.getCallingClass().isAssignableFrom(RegisteredTypeDetails.class)
      : "Only RegisteredTypeDetails is allowed to set details. Was called from " + Utils.Caller.getCallingClass().getName();

    this.details = details;
  }

  public String getParameterizedIdent() {
    return details.getParameterizedIdent(this);
  }

  public abstract boolean validate(Object value);
}

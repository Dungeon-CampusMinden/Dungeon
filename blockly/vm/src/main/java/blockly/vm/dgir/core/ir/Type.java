package blockly.vm.dgir.core.ir;

import blockly.vm.dgir.core.Utils;
import blockly.vm.dgir.core.detail.RegisteredTypeDetails;
import blockly.vm.dgir.core.detail.TypeDetails;
import blockly.vm.dgir.core.serialization.TypeDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import tools.jackson.databind.annotation.JsonDeserialize;

// We have to use the deserializer because we cant use @JsonCreator on static methods and therefore can put the logic
// directly in this class.
@JsonDeserialize(using = TypeDeserializer.class)
public abstract class Type {
  @JsonIgnore
  private TypeDetails details;

  // Serialize the type as a parameterized ident string
  @JsonValue
  public String getParameterizedIdent() {
    return details.getParameterizedIdent(this);
  }

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

  public void setDetails(TypeDetails details) {
    assert Utils.Caller.getCallingClass().isAssignableFrom(RegisteredTypeDetails.class)
      : "Only RegisteredTypeDetails is allowed to set details. Was called from " + Utils.Caller.getCallingClass().getName();

    this.details = details;
  }

  public abstract boolean validate(Object value);

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Type other) && this.getParameterizedIdent().equals(other.getParameterizedIdent());
  }

  @Override
  public int hashCode() {
    return getParameterizedIdent().hashCode();
  }
}

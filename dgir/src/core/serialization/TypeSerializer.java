package core.serialization;

import core.ir.Type;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class TypeSerializer extends StdSerializer<Type> {
  public TypeSerializer() {
    super(Type.class);
  }

  public TypeSerializer(Class<?> t) {
    super(t);
  }

  @Override
  public void serialize(Type value, JsonGenerator gen, SerializationContext provider)
      throws JacksonException {
    gen.writeString(value.getDetails().getParameterizedIdent(value));
  }
}

package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.NamedAttribute;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class NamedAttributeSerializer extends StdSerializer<NamedAttribute> {
  public NamedAttributeSerializer() {
    super(NamedAttribute.class);
  }

  public NamedAttributeSerializer(Class<?> t) {
    super(t);
  }

  @Override
  public void serialize(NamedAttribute value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
    gen.writeStartObject();
    gen.writePOJOProperty("name", value.getName());
    gen.writePOJOProperty("attribute", value.getAttribute());
    gen.writeEndObject();
  }
}

package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.Attribute;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class AttributeSerializer extends StdSerializer<Attribute> {
  public AttributeSerializer() {
    super(Attribute.class);
  }

  public AttributeSerializer(Class<Attribute> t) {
    super(t);
  }


  @Override
  public void serialize(Attribute value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
    gen.writeStartObject();
    gen.writeStringProperty("ident", value.getDetails().getIdent());
    gen.writePOJOProperty("type", value.getType());
    if (value.getStorage() != null)
      gen.writePOJOProperty("value", value.getStorage());
    gen.writeEndObject();
  }
}

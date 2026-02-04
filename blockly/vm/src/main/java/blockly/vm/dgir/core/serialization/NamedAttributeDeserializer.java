package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.NamedAttribute;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class NamedAttributeDeserializer extends StdDeserializer<NamedAttribute> {
  public NamedAttributeDeserializer() {
    this(NamedAttribute.class);
  }

  public NamedAttributeDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public NamedAttribute deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
    throw new RuntimeException("NamedAttributeDeserializer not implemented yet");
  }
}

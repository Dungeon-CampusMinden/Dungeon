package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.Attribute;
import tools.jackson.databind.deser.std.StdDeserializer;

public class AttributeDeserializer extends StdDeserializer<Attribute> {
  public AttributeDeserializer() {
    this(Attribute.class);
  }

  public AttributeDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Attribute deserialize(tools.jackson.core.JsonParser jp, tools.jackson.databind.DeserializationContext ctxt) {
    throw new RuntimeException("AttributeDeserializer not implemented yet");
  }
}

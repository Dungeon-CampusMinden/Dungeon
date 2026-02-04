package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.Attribute;
import blockly.vm.dgir.core.NamedAttribute;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
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
    JsonNode node = p.readValueAsTree();
    // Get the name of this named attribute.
    String name = node.get("name").asString();
    // Deserialize the attribute field as an Attribute object.
    JsonNode attributeNode = node.get("attribute");
    Attribute attribute = ctxt.readTreeAsValue(attributeNode, Attribute.class);
    return new NamedAttribute(name, attribute);
  }
}

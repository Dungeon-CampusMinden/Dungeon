package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.Type;
import tools.jackson.databind.deser.std.StdDeserializer;

public class TypeDeserializer extends StdDeserializer<Type> {
  public TypeDeserializer() {
    this(Type.class);
  }

  public TypeDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Type deserialize(tools.jackson.core.JsonParser jp, tools.jackson.databind.DeserializationContext ctxt) {
    throw new RuntimeException("TypeDeserializer not implemented yet");
  }
}

package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.Type;
import blockly.vm.dgir.core.TypeDetails;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class TypeDeserializer extends StdDeserializer<Type> {
  public TypeDeserializer() {
    this(Type.class);
  }

  public TypeDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Type deserialize(JsonParser jp, DeserializationContext ctxt) {
    return TypeDetails.fromParameterizedIdent(jp.getString());
  }
}

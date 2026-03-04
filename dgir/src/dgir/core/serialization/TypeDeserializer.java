package dgir.core.serialization;

import dgir.core.ir.TypeDetails;
import dgir.core.ir.Type;
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

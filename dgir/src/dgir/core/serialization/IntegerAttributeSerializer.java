package dgir.core.serialization;

import dgir.dialect.builtin.BuiltinAttrs;
import dgir.dialect.builtin.BuiltinTypes;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.jsontype.TypeSerializer;
import tools.jackson.databind.ser.std.StdSerializer;

public class IntegerAttributeSerializer extends StdSerializer<BuiltinAttrs.IntegerAttribute> {
  public IntegerAttributeSerializer() {
    super(BuiltinAttrs.IntegerAttribute.class);
  }

  public IntegerAttributeSerializer(Class<?> t) {
    super(t);
  }

  @Override
  public void serialize(
      BuiltinAttrs.IntegerAttribute value, JsonGenerator gen, SerializationContext provider)
      throws JacksonException {
    writeIntegerAttribute(value, gen);
  }

  @Override
  public void serializeWithType(
      BuiltinAttrs.IntegerAttribute value,
      JsonGenerator gen,
      SerializationContext provider,
      TypeSerializer typeSer)
      throws JacksonException {
    // Type id comes from the existing "ident" property, so normal object serialization is enough.
    writeIntegerAttribute(value, gen);
  }

  private static void writeIntegerAttribute(BuiltinAttrs.IntegerAttribute value, JsonGenerator gen)
      throws JacksonException {
    gen.writeStartObject();
    gen.writeStringProperty("ident", value.getIdent());
    gen.writePOJOProperty("type", value.getType());
    BuiltinTypes.IntegerT integerType = (BuiltinTypes.IntegerT) value.getType();
    if (integerType.equals(BuiltinTypes.IntegerT.BOOL)) {
      gen.writeBooleanProperty("value", value.getValue().byteValue() != 0);
    } else {
      if (integerType.isSigned()) {
        gen.writeNumberProperty("value", value.getValue().longValue());
      } else {
        String unsignedValue =
            String.valueOf(
                switch (value.getValue()) {
                  case Byte b -> Byte.toUnsignedInt(b);
                  case Short s -> Short.toUnsignedInt(s);
                  case Integer i -> Integer.toUnsignedLong(i);
                  case Long l -> Long.toUnsignedString(l);
                  default ->
                      throw new IllegalStateException(
                          "Unexpected value type: " + value.getValue().getClass());
                });
        gen.writeRaw(",");
        if (gen.getPrettyPrinter() != null) gen.getPrettyPrinter().beforeObjectEntries(gen);
        gen.writeRaw("\"value\" : " + unsignedValue);
      }
    }
    gen.writeEndObject();
  }
}

package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = TypeSerializer.class)
@JsonDeserialize(using = TypeDeserializer.class)
public abstract non-sealed class Type implements IIdentifiableType, ITypeLike {
  public abstract boolean validate(Object value);

  @Override
  public Type getType(){
    return this;
  }
}

class TypeSerializer extends StdSerializer<Type> {
  public TypeSerializer() {
    super(Type.class);
  }

  public TypeSerializer(Class<Type> t) {
    super(t);
  }

  @Override
  public void serialize(Type value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
    gen.writeString(value.getIdent());
  }
}

class TypeDeserializer extends StdDeserializer<Type> {
  public TypeDeserializer() {
    super(Type.class);
  }

  public TypeDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Type deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
    String typeStr = p.getString();
    try {
      return DialectRegistry.getTypeInstance(typeStr);
    } catch (IllegalArgumentException e) {
      throw ctxt.invalidTypeIdException(_valueType, typeStr, e.getMessage());
    }
  }
}

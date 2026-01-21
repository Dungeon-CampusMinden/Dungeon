package blockly.vm.dgir.core.type;

import blockly.vm.dgir.core.DialectRegistry;
import blockly.vm.dgir.core.IDialect;
import com.fasterxml.jackson.annotation.*;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Optional;

@JsonSerialize(using = TypeSerializer.class)
@JsonDeserialize(using = TypeDeserializer.class)
public abstract class Type {
  private final String ident;

  public Type(Class<? extends IDialect> dialectClass, String ident) {
    var dialect = DialectRegistry.getDialect(dialectClass);
    if (dialect.getNamespace().isEmpty()) this.ident = ident;
    else this.ident = dialect.getNamespace() + "." + ident;
  }

  public IDialect getDialect() {
    return DialectRegistry.getTypeDialect(getIdent());
  }

  public String getIdent() {
    return ident;
  }

  public abstract boolean validate(Object value);
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

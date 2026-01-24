package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.ser.std.StdSerializer;

import java.lang.reflect.InvocationTargetException;

@JsonSerialize(using = TypeSerializer.class)
@JsonDeserialize(using = TypeDeserializer.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public abstract class Type {
  private TypeName impl;

  // Every type should be default constructible.
  public Type(){
  }

  public Type(TypeName typeName){
    this.impl = typeName;
  }

  public abstract TypeName.Impl createImpl();

  public TypeName getName(){
    return impl;
  }

  void setImpl(TypeName impl) {
    this.impl = impl;
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
    gen.writeString(value.getName().getName());
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
      TypeName typeName = DGIRContext.registeredTypesByName.get(typeStr);
      var constructor = typeName.getType().getConstructor(TypeName.class);
      return constructor.newInstance(typeName);
    } catch (IllegalArgumentException e) {
      throw ctxt.invalidTypeIdException(_valueType, typeStr, e.getMessage());
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}

package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.DialectRegistry;
import blockly.vm.dgir.core.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;
import tools.jackson.databind.type.TypeFactory;

import java.io.Serial;
import java.util.Optional;

public class TypeTypeIdResolver
  extends TypeIdResolverBase
  implements java.io.Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @Override
  public String idFromValue(DatabindContext ctxt, Object value) throws JacksonException {
    return "";
  }

  @Override
  public String idFromValueAndType(DatabindContext ctxt, Object value, Class<?> suggestedType) throws JacksonException {
    return "";
  }

  @Override
  public JsonTypeInfo.Id getMechanism() {
    return JsonTypeInfo.Id.CUSTOM;
  }

  @Override
  public JavaType typeFromId(DatabindContext context, String id) throws JacksonException {
    DeserializationContext deserializationContext = null;
    if (context instanceof DeserializationContext) {
      deserializationContext = (DeserializationContext) context;
    }

    Optional<Class<? extends Type>> type = DialectRegistry.getType(id);
    if (type.isPresent()) {
      return TypeFactory.unsafeSimpleType(type.get());
    }

    if (deserializationContext != null) {
      throw deserializationContext.invalidTypeIdException(_baseType, id,
        "Could not resolve type for id: " + id + "\nMake sure it is registered in the dialect registry!");
    }
    return null;
  }
}

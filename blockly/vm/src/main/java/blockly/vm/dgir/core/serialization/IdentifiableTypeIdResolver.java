package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.DialectRegistry;
import blockly.vm.dgir.core.IIdentifiableType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;
import tools.jackson.databind.type.TypeFactory;

import java.io.Serial;

public class IdentifiableTypeIdResolver extends TypeIdResolverBase
  implements java.io.Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @Override
  public String idFromValue(DatabindContext ctxt, Object value) throws JacksonException {
    if (value instanceof IIdentifiableType)
      return ((IIdentifiableType) value).getIdent();
    throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName() + "\nMake sure it implements IIdentifiableType");
  }

  @Override
  public String idFromValueAndType(DatabindContext ctxt, Object value, Class<?> suggestedType) throws JacksonException {
    return idFromValue(ctxt, value);
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

    try {
      return TypeFactory.unsafeSimpleType(DialectRegistry.getTypeClass(id));
    } catch (IllegalArgumentException e) {
      if (deserializationContext != null) {
        throw deserializationContext.invalidTypeIdException(_baseType, id, e.getMessage());
      }
    }
    return null;
  }
}

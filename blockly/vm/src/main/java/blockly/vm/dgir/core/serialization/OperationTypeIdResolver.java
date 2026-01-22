package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.DialectRegistry;
import blockly.vm.dgir.core.Operation;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;
import tools.jackson.databind.type.TypeFactory;

import java.io.Serial;
import java.util.Optional;

/**
 * Resolves the concrete operation class based on namespace.name identifiers.
 */
public class OperationTypeIdResolver
  extends TypeIdResolverBase
  implements java.io.Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  public OperationTypeIdResolver() {
    super(TypeFactory.unsafeSimpleType(Operation.class));
  }

  @Override
  public String idFromValue(DatabindContext ctxt, Object value) {
    return ((Operation) value).getIdent();
  }

  @Override
  public String idFromValueAndType(DatabindContext ctxt, Object value, Class<?> suggestedType) {
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
      return TypeFactory.unsafeSimpleType(DialectRegistry.getOpType(id));
    } catch (IllegalArgumentException e) {
      if (deserializationContext != null) {
        throw deserializationContext.invalidTypeIdException(_baseType, id, e.getMessage());
      }
    }
    return null;
  }
}

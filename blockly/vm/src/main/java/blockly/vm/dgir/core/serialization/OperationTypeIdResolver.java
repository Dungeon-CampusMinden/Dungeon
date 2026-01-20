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

import java.util.Optional;

/**
 * Resolves the concrete operation class based on namespace.name identifiers.
 */
public class OperationTypeIdResolver
  extends TypeIdResolverBase
  implements java.io.Serializable
{
  private static final long serialVersionUID = 1L;

  public OperationTypeIdResolver() {
    super(TypeFactory.unsafeSimpleType(Operation.class));
  }

  @Override
  public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }

  @Override
  public String idFromValue(DatabindContext ctxt, Object value) {
    return ((Operation) value).getNamespace() + "." + ((Operation) value).getName();
  }

  @Override
  public String idFromValueAndType(DatabindContext ctxt, Object value, Class<?> suggestedType) {
    return idFromValue(ctxt, value);
  }

  @Override
  public JavaType typeFromId(DatabindContext ctxt, String id) throws JacksonException {
    DeserializationContext deserializationContext = null;
    if (ctxt instanceof DeserializationContext) {
      deserializationContext = (DeserializationContext) ctxt;
    }

    Optional<Class<? extends Operation>> type = DialectRegistry.getType(id);
    if (type.isPresent()) {
      return TypeFactory.unsafeSimpleType(type.get());
    }

    if (deserializationContext != null) {
      throw deserializationContext.invalidTypeIdException(_baseType, id,
        "Could not resolve operation type for id: " + id + "\nMake sure it is registered in the dialect registry!");
    }
    return null;
  }
}

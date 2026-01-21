package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.Value;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class ValueTypeIdResolver extends TypeIdResolverBase
  implements java.io.Serializable {
  @Override
  public String idFromValue(DatabindContext ctxt, Object value) throws JacksonException {
    if (value instanceof Value)
      return Value.class.getSimpleName();

    throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
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
    if (id.equals(Value.class.getSimpleName()))
      return context.constructType(Value.class);

    throw new IllegalArgumentException("Unsupported value type: " + id);
  }
}

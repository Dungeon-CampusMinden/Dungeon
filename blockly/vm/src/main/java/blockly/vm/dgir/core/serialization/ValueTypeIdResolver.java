package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.ConstantValue;
import blockly.vm.dgir.core.DynamicValue;
import blockly.vm.dgir.core.ValueRef;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class ValueTypeIdResolver extends TypeIdResolverBase
  implements java.io.Serializable {
  @Override
  public String idFromValue(DatabindContext ctxt, Object value) throws JacksonException {
    if (value instanceof DynamicValue)
      return DynamicValue.class.getSimpleName();
    else if (value instanceof ConstantValue)
      return ConstantValue.class.getSimpleName();
    else if (value instanceof ValueRef)
      return ValueRef.class.getSimpleName();

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
    if (id.equals(DynamicValue.class.getSimpleName()))
      return context.constructType(DynamicValue.class);
    else if (id.equals(ConstantValue.class.getSimpleName()))
      return context.constructType(ConstantValue.class);
    else if (id.equals(ValueRef.class.getSimpleName()))
      return context.constructType(ValueRef.class);

    throw new IllegalArgumentException("Unsupported value type: " + id);
  }
}

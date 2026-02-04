package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.Attribute;
import blockly.vm.dgir.core.RegisteredAttributeDetails;
import blockly.vm.dgir.core.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;
import tools.jackson.databind.type.TypeFactory;

public class AttributeTypeIdResolver extends TypeIdResolverBase {
  public AttributeTypeIdResolver() {
    super(TypeFactory.createDefaultInstance().constructType(Attribute.class));
  }

  @Override
  public String idFromValue(DatabindContext ctxt, Object value) throws JacksonException {
    if (value instanceof Attribute attribute) {
      return attribute.getDetails().getIdent();
    }
    throw new JacksonException("Cannot resolve type id for value: " + value) {
    };
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
    var optionalCls = RegisteredAttributeDetails.lookup(id);
    if (optionalCls.isEmpty()) {
      throw new JacksonException("Unknown attribute type id: " + id) {
      };
    }
    return context.getTypeFactory().constructType(optionalCls.get().getType());
  }
}

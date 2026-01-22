package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.annotation.JsonTypeIdResolver;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;
import tools.jackson.databind.type.TypeFactory;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "ident")
@JsonTypeIdResolver(AttributeIdResolver.class)
@JsonPropertyOrder({"ident", "type", "value"})
public abstract class Attribute implements IIdentifiableType, Serializable {
  private final Type type;

  public Attribute() {
    this.type = null;
  }

  @JsonCreator
  public Attribute(@JsonProperty("type") Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}

class AttributeIdResolver extends TypeIdResolverBase implements java.io.Serializable {
  @Override
  public String idFromValue(DatabindContext ctxt, Object value) throws JacksonException {
    if (value instanceof Attribute)
      return ((Attribute) value).getIdent();

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
    try {
      return TypeFactory.unsafeSimpleType(DialectRegistry.getAttributeType(id));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }
}

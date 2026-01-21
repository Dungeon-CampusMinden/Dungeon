package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.Type;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.annotation.JsonTypeIdResolver;
import tools.jackson.databind.jsontype.impl.TypeIdResolverBase;
import tools.jackson.databind.type.TypeFactory;

import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "name")
@JsonTypeIdResolver(AttributeIdResolver.class)
public class Attribute implements Serializable {
  /**
   * The type name of the attribute.
   */
  private final String name;
  private final Type type;

  public Attribute() {
    this.name = null;
    this.type = null;
  }

  public Attribute(Class<? extends IDialect> dialectClass, String name, Type type) {
    this.name = Utility.getFullName(dialectClass, name);
    this.type = type;
  }

  @JsonCreator
  public Attribute(@JsonProperty("name") String name, @JsonProperty("type") Type type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  @JsonIgnore
  public IDialect getDialect() {
    return DialectRegistry.getAttributeDialect(name);
  }

  public Type getType() {
    return type;
  }
}

class AttributeIdResolver extends TypeIdResolverBase implements java.io.Serializable {
  @Override
  public String idFromValue(DatabindContext ctxt, Object value) throws JacksonException {
    if (value instanceof Attribute)
      return ((Attribute) value).getName();

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

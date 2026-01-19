package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.DatabindContext;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.jsontype.*;
import tools.jackson.databind.jsontype.impl.*;

import java.util.Collection;


/**
 * Always includes namespace+name as an existing property and relies on the registry for
 * picking the concrete implementation.
 */
public class OperationTypeResolverBuilder extends StdTypeResolverBuilder {
  private static final long serialVersionUID = 1L;

  public OperationTypeResolverBuilder() {
    JsonTypeInfo.Value settings = JsonTypeInfo.Value.construct(
      JsonTypeInfo.Id.CUSTOM,
      JsonTypeInfo.As.EXISTING_PROPERTY,
      "operation",
      null,
      true,
      false
    );
    init(settings, new OperationTypeIdResolver());
  }
}

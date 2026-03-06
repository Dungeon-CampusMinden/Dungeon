package dgir.core.serialization;

import dgir.core.ir.TypeDetails;
import dgir.core.ir.Type;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializes a {@link Type} from its parameterized ident string representation.
 *
 * <p>Input must be a non-empty JSON string. Unknown or unregistered type idents are surfaced as
 * input mismatches with descriptive error text.
 */
public class TypeDeserializer extends StdDeserializer<Type> {
  public TypeDeserializer() {
    this(Type.class);
  }

  public TypeDeserializer(Class<?> vc) {
    super(vc);
  }

  /**
   * Deserialize and validate a type ident string before resolving it through {@link TypeDetails}.
   */
  @Override
  public Type deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
    JsonNode node = jp.readValueAsTree();
    if (node == null || node.isNull()) {
      return ctxt.reportInputMismatch(Type.class, "Type value must not be null.");
    }
    if (!node.isString()) {
      return ctxt.reportInputMismatch(Type.class, "Type value must be a string.");
    }

    String parameterizedIdent = node.asString().trim();
    if (parameterizedIdent.isEmpty()) {
      return ctxt.reportInputMismatch(Type.class, "Type string must not be empty.");
    }

    try {
      return TypeDetails.fromParameterizedIdent(parameterizedIdent);
    } catch (IllegalArgumentException ex) {
      return ctxt.reportInputMismatch(Type.class, ex.getMessage());
    }
  }
}

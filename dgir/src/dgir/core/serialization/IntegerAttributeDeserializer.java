package dgir.core.serialization;

import dgir.core.ir.Type;
import dgir.dialect.builtin.BuiltinAttrs;
import dgir.dialect.builtin.BuiltinTypes;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializes builtin {@code integerAttr} payloads.
 *
 * <p>Required fields are {@code type} and {@code value}. The deserializer validates both fields and
 * reports malformed input through {@link DeserializationContext#reportInputMismatch}.
 */
public class IntegerAttributeDeserializer extends StdDeserializer<BuiltinAttrs.IntegerAttribute> {
  public IntegerAttributeDeserializer() {
    this(BuiltinAttrs.IntegerAttribute.class);
  }

  protected IntegerAttributeDeserializer(Class<?> vc) {
    super(vc);
  }

  /** Deserialize an integer attribute from JSON and validate the integer type/value pairing. */
  @Override
  public BuiltinAttrs.IntegerAttribute deserialize(JsonParser p, DeserializationContext ctxt)
      throws JacksonException {
    JsonNode node = p.readValueAsTree();

    JsonNode typeNode = node.get("type");
    if (typeNode == null || typeNode.isNull()) {
      return ctxt.reportInputMismatch(
          BuiltinAttrs.IntegerAttribute.class, "Missing required field 'type'.");
    }

    Type parsedType = ctxt.readTreeAsValue(typeNode, Type.class);
    if (!(parsedType instanceof BuiltinTypes.IntegerT integerType)) {
      return ctxt.reportInputMismatch(
          BuiltinAttrs.IntegerAttribute.class,
          "Field 'type' must be an integer type but was '%s'.",
          parsedType == null ? "null" : parsedType.getParameterizedIdent());
    }

    JsonNode valueNode = node.get("value");
    if (valueNode == null || valueNode.isNull()) {
      return ctxt.reportInputMismatch(
          BuiltinAttrs.IntegerAttribute.class, "Missing required field 'value'.");
    }

    long value = parseValue(valueNode, integerType, ctxt);
    return new BuiltinAttrs.IntegerAttribute(value, integerType);
  }

  /**
   * Parse the numeric payload according to the concrete integer type.
   *
   * <p>For {@code bool}, values are normalized to {@code 0} or {@code 1}.
   */
  private static long parseValue(
      JsonNode valueNode, BuiltinTypes.IntegerT integerType, DeserializationContext ctxt)
      throws JacksonException {
    if (integerType.equals(BuiltinTypes.IntegerT.BOOL)) {
      if (valueNode.isBoolean()) {
        return (valueNode.booleanValue() ? 1 : 0);
      }
      if (valueNode.isIntegralNumber()) {
        return (valueNode.longValue() == 0 ? 0 : 1);
      }
      return ctxt.reportInputMismatch(
          BuiltinAttrs.IntegerAttribute.class,
          "Invalid bool integer value. Expected boolean, or integral number.");
    }

    if (valueNode.isIntegralNumber()) {
      return valueNode.longValue();
    }

    return ctxt.reportInputMismatch(
        BuiltinAttrs.IntegerAttribute.class,
        "Invalid integer value type. Expected integral number.");
  }
}

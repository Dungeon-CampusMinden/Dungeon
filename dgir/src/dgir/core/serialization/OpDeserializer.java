package dgir.core.serialization;

import dgir.core.ir.Op;
import dgir.core.ir.Operation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Deserializes an {@link Op} by first decoding its wrapped {@link Operation} payload.
 *
 * <p>The JSON value must be an object that can be deserialized as an operation.
 */
public class OpDeserializer extends StdDeserializer<Op> {
  public OpDeserializer() {
    this(Op.class);
  }

  public OpDeserializer(Class<?> vc) {
    super(vc);
  }

  /**
   * Deserialize an {@link Op} and convert structural or semantic issues into input mismatch errors.
   */
  @Override
  public Op deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
    JsonNode node = p.readValueAsTree();
    if (node == null || node.isNull()) {
      return ctxt.reportInputMismatch(Op.class, "Op value must not be null.");
    }
    if (!node.isObject()) {
      return ctxt.reportInputMismatch(Op.class, "Op value must be a JSON object.");
    }

    try {
      Operation operation = ctxt.readTreeAsValue(node, Operation.class);
      return operation.asOp();
    } catch (IllegalArgumentException ex) {
      return ctxt.reportInputMismatch(Op.class, ex.getMessage());
    }
  }
}

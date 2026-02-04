package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.Op;
import blockly.vm.dgir.core.Operation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class OpDeserializer extends StdDeserializer<Op> {
  public OpDeserializer() {
    this(Op.class);
  }

  public OpDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Op deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
    // Deserialize the object as an operation and create a new Op object with the deserialized value.
    Operation operation = p.readValueAs(Operation.class);
    return operation.asOp();
  }
}

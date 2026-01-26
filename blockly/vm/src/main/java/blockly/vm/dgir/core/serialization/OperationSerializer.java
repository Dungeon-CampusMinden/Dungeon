package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.Operation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class OperationSerializer extends StdSerializer<Operation> {
  public OperationSerializer() {
    super(Operation.class);
  }

  public OperationSerializer(Class<?> t) {
    super(t);
  }

  @Override
  public void serialize(Operation value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
    assert value.getDetails() != null : "Operation details must be set before serialization.";

    gen.writeStartObject();
    gen.writeStringProperty("ident", value.getDetails().getIdent());
    if (!value.getOperands().isEmpty())
      gen.writePOJOProperty("operands", value.getOperands());
    if (!value.getAttributes().isEmpty()) {
      // Convert the map to a list of attributes.
      gen.writePOJOProperty("attributes", value.getAttributes().values());
    }
    if (value.getOutput() != null)
      gen.writePOJOProperty("output", value.getOutput());
    if (!value.getRegions().isEmpty())
      gen.writePOJOProperty("regions", value.getRegions());
    gen.writeEndObject();
  }
}

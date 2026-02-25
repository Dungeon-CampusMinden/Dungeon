package core.serialization;

import core.ir.Operation;
import core.ir.Location;
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
  public void serialize(Operation value, JsonGenerator gen, SerializationContext provider)
      throws JacksonException {
    gen.writeStartObject();
    gen.writeStringProperty("ident", value.getDetails().ident());
    if (!value.getOperands().isEmpty()) gen.writePOJOProperty("operands", value.getOperands());
    if (!value.getBlockOperands().isEmpty())
      gen.writePOJOProperty("successors", value.getBlockOperands());
    if (!value.getAttributes().isEmpty()) {
      // Convert the map to a list of attributes.
      gen.writePOJOProperty("attributes", value.getAttributes().values());
    }
    if (value.getOutput().isPresent()) gen.writePOJOProperty("output", value.getOutput());
    if (!value.getRegions().isEmpty()) gen.writePOJOProperty("regions", value.getRegions());
    if (!value.getLocation().equals(Location.UNKNOWN))
      gen.writeStringProperty("loc", value.getLocation().toString());
    gen.writeEndObject();
  }
}

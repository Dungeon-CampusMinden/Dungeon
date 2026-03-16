package dgir.core.serialization;

import dgir.core.ir.Op;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class OpSerializer extends StdSerializer<Op> {
  public OpSerializer() {
    super(Op.class);
  }

  public OpSerializer(Class<?> t) {
    super(t);
  }

  @Override
  public void serialize(Op value, JsonGenerator gen, SerializationContext provider)
      throws JacksonException {
    gen.writePOJO(value.getOperation());
  }
}

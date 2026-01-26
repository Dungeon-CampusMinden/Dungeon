package blockly.vm.dgir.dialect.func.serialization;

import blockly.vm.dgir.dialect.func.types.FuncType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class FuncTypeSerializer extends StdSerializer<FuncType> {
  public FuncTypeSerializer() {
    super(FuncType.class);
  }

  public FuncTypeSerializer(Class<?> t) {
    super(t);
  }

  @Override
  public void serialize(FuncType value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
    String fullIdent = value.getDetails().getIdent();
    fullIdent += "<";

    ObjectMapper typeMapper = new ObjectMapper();

    // Write input params
    {
      fullIdent += "(";
      fullIdent += String.join(", ",
          value.getInputs().stream().map(typeMapper::writeValueAsString).toList());
      fullIdent += ")";
    }

    fullIdent += " -> ";

    {
      // Write output params
      if (value.getOutput() != null) {
        fullIdent += typeMapper.writeValueAsString(value.getOutput());
      }else {
        fullIdent += "()";
      }
    }

    fullIdent += ">";

    gen.writeString(fullIdent);
  }
}

package dialect.builtin;

import blockly.vm.dgir.core.IOperation;
import blockly.vm.dgir.core.OperationTypeResolverBuilder;
import blockly.vm.dgir.core.OperationTypeValidator;
import blockly.vm.dgir.dialect.builtin.ProgramOp;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuiltinTests {
  @Test
  public void programOp() {
    ProgramOp op = new ProgramOp();

    ObjectMapper mapper = JsonMapper.builder()
      .setDefaultTyping(new OperationTypeResolverBuilder())
      .activateDefaultTyping(new OperationTypeValidator())
      .build();

    String result = mapper.writeValueAsString(op);
    System.out.println(result);
    assertEquals("{\"operation\":\"builtin.program\",\"containingRegion\":null}", result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    System.out.println(restoredOp);
  }
}

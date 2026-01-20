package dialect.builtin;

import blockly.vm.dgir.core.DialectRegistry;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.builtin.ProgramOp;
import blockly.vm.dgir.dialect.func.Func;
import blockly.vm.dgir.dialect.func.FuncOp;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuiltinTests {
  @Test
  public void emptyProgramOp() {
    ObjectMapper mapper = JsonMapper.builder()
      .build();

    DialectRegistry.registerDialect(Builtin.class);
    ProgramOp op = new ProgramOp();

    String result = mapper.writeValueAsString(op);
    System.out.println(result);
    assertEquals("{\"operation\":\"program\",\"region\":{\"blocks\":[{}]}}", result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    assertEquals(restoredOp.getRegion(), restoredOp.getRegion().getOrCreateDefaultBlock().getParent());
  }

  @Test
  public void simpleProgramOp() {
    ObjectMapper mapper = JsonMapper.builder()
      .build();

    DialectRegistry.registerDialect(Builtin.class);
    DialectRegistry.registerDialect(Func.class);

    ProgramOp op = new ProgramOp();
    var programRegion = op.getRegion();
    var block = programRegion.getOrCreateDefaultBlock();
    var funcOp = new FuncOp();
    block.addOperation(funcOp);

    String result = mapper.writeValueAsString(op);
    System.out.println(result);
    assertEquals("{\"operation\":\"program\",\"region\":{\"blocks\":[{}]}}", result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
  }
}

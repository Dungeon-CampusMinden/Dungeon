package dialect.builtin;

import blockly.vm.dgir.core.ConstantValue;
import blockly.vm.dgir.core.DialectRegistry;
import blockly.vm.dgir.core.type.Int32_t;
import blockly.vm.dgir.core.type.String_t;
import blockly.vm.dgir.dialect.arith.Arith;
import blockly.vm.dgir.dialect.arith.ConstantOp;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.builtin.ProgramOp;
import blockly.vm.dgir.dialect.func.Func;
import blockly.vm.dgir.dialect.func.FuncOp;
import blockly.vm.dgir.dialect.io.IO;
import blockly.vm.dgir.dialect.io.PrintOp;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuiltinTests {
  @Test
  public void emptyProgramOp() {
    ObjectMapper mapper = JsonMapper.builder()
      .enable(tools.jackson.databind.SerializationFeature.INDENT_OUTPUT)
      .build();

    DialectRegistry.registerDialect(Builtin.class);
    ProgramOp op = new ProgramOp();

    String result = mapper.writeValueAsString(op);
    System.out.println(result);
    assertEquals(
      """
          {
            "op" : "program",
            "region" : {
              "blocks" : [ {
                "label" : "blk_0",
                "operations" : [ ]
              } ]
            }
          }""", result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    assertEquals(restoredOp.getRegion(), restoredOp.getRegion().getOrCreateDefaultBlock().getParent());
  }

  @Test
  public void simpleProgramOp() {
    ObjectMapper mapper = JsonMapper.builder()
      .enable(tools.jackson.databind.SerializationFeature.INDENT_OUTPUT)
      .build();

    DialectRegistry.registerDialect(Builtin.class);
    DialectRegistry.registerDialect(Func.class);
    DialectRegistry.registerDialect(IO.class);
    DialectRegistry.registerDialect(Arith.class);

    ProgramOp op = new ProgramOp();
    var programRegion = op.getRegion();
    var progBlock = programRegion.getOrCreateDefaultBlock();

    var funcOp = new FuncOp();
    funcOp.setLabel("func");
    var funcRegion = funcOp.getRegion();
    var funcBlock = funcRegion.getOrCreateDefaultBlock();
    progBlock.addOperation(funcOp);


    var textOp = new ConstantOp();
    textOp.setValue(new ConstantValue("%x", Int32_t.INSTANCE, 42));
    funcBlock.addOperation(textOp);

    var printOp = new PrintOp();
    printOp.getArguments().add(new ConstantValue("CONSTANT", String_t.INSTANCE, "The answer is: "));
    printOp.getArguments().add(textOp.getValue());
    funcBlock.addOperation(printOp);

    String result = mapper.writeValueAsString(op);
    System.out.println(result);
    assertEquals("{\"operation\":\"program\",\"region\":{\"blocks\":[{}]}}", result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
  }
}

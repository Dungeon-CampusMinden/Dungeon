package dialect.builtin;

import blockly.vm.dgir.core.ConstantValue;
import blockly.vm.dgir.core.DialectRegistry;
import blockly.vm.dgir.core.ValueRef;
import blockly.vm.dgir.core.serialization.Utility;
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
    ObjectMapper mapper = Utility.getMapper(false, true);

    ProgramOp op = new ProgramOp();

    String result = mapper.writeValueAsString(op);
    assertEquals("{\"op\":\"program\",\"output\":null,\"region\":{\"blocks\":[{\"label\":\"blk_0\",\"operations\":[]}]}}", result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    assertEquals(restoredOp.getRegion(), restoredOp.getRegion().getOrCreateDefaultBlock().parent);
  }

  @Test
  public void simpleProgramOp() {
    ObjectMapper mapper = Utility.getMapper(true, true);

    ProgramOp op = new ProgramOp();
    var programRegion = op.getRegion();
    var progBlock = programRegion.getOrCreateDefaultBlock();

    var funcOp = new FuncOp();
    funcOp.setLabel("func");
    var funcRegion = funcOp.getRegion();
    var funcBlock = funcRegion.getOrCreateDefaultBlock();
    progBlock.addOperation(funcOp);


    var textOp = new ConstantOp();
    textOp.setValue(new ConstantValue(Int32_t.INSTANCE, 42));
    funcBlock.addOperation(textOp);

    var printOp = new PrintOp();
    printOp.getArguments().add(new ConstantValue(String_t.INSTANCE, "The answer is: "));
    printOp.getArguments().add(new ValueRef(textOp.getOutput().get()));
    funcBlock.addOperation(printOp);

    String result = mapper.writeValueAsString(op);
    System.out.println(result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    assertEquals(mapper.writeValueAsString(op), mapper.writeValueAsString(restoredOp));

    assertEquals(restoredOp.getRegion(), restoredOp.getRegion().getOrCreateDefaultBlock().parent);
  }
}

package dialect.builtin;

import blockly.vm.dgir.core.ConstantValue;
import blockly.vm.dgir.core.ValueRef;
import blockly.vm.dgir.core.serialization.Utility;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;
import blockly.vm.dgir.dialect.builtin.types.StringT;
import blockly.vm.dgir.dialect.arith.ConstantOp;
import blockly.vm.dgir.dialect.builtin.ProgramOp;
import blockly.vm.dgir.dialect.func.FuncOp;
import blockly.vm.dgir.dialect.io.PrintOp;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BuiltinTests {
  @Test
  public void emptyProgramOp() {
    ObjectMapper mapper = Utility.getMapper(false, true);

    ProgramOp op = new ProgramOp();

    String result = mapper.writeValueAsString(op);
    System.out.println(result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    assertEquals(mapper.writeValueAsString(op), mapper.writeValueAsString(restoredOp));
  }

  @Test
  public void simpleProgramOp() {
    ObjectMapper mapper = Utility.getMapper(true, true);

    ProgramOp op = new ProgramOp();
    var programRegion = op.getRegion();
    var progBlock = programRegion.getOrCreateDefaultBlock();

    var funcOp = new FuncOp("main");
    var funcRegion = funcOp.getRegion();
    var funcBlock = funcRegion.getOrCreateDefaultBlock();
    progBlock.operations.add(funcOp);

    var textOp = new ConstantOp(new ConstantValue(IntegerT.INT32, 42));
    funcBlock.operations.add(textOp);

    var textConst = new ConstantValue(StringT.INSTANCE, "The answer is: ");
    var valueRef = new ValueRef(textOp.getOutput());
    funcBlock.operations.add(new PrintOp(List.of(textConst, valueRef)));

    String result = mapper.writeValueAsString(op);
    System.out.println(result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    System.out.println(mapper.writeValueAsString(restoredOp));
    assertEquals(mapper.writeValueAsString(op), mapper.writeValueAsString(restoredOp));

  }
}

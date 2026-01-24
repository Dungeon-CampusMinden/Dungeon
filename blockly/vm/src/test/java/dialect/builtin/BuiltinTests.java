package dialect.builtin;

import blockly.vm.dgir.core.NamedAttribute;
import blockly.vm.dgir.core.serialization.Utility;
import blockly.vm.dgir.dialect.builtin.attributes.IntegerAttribute;
import blockly.vm.dgir.dialect.builtin.attributes.StringAttribute;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;
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
    ObjectMapper mapper = Utility.getMapper(true, true);

    ProgramOp op = new ProgramOp(true);

    String result = mapper.writeValueAsString(op);
    System.out.println(result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    assertEquals(mapper.writeValueAsString(op), mapper.writeValueAsString(restoredOp));
  }

  @Test
  public void simpleProgramOp() {
    ObjectMapper mapper = Utility.getMapper(true, true);

    ProgramOp op = new ProgramOp(true);
    var programRegion = op.getOperation().getRegions().getFirst();
    var progBlock = programRegion.getOrCreateDefaultBlock();

    var funcOp = new FuncOp("main");
    var funcRegion = funcOp.getOperation().getRegions().getFirst();
    var funcBlock = funcRegion.getOrCreateDefaultBlock();
    progBlock.addOperation(funcOp.getOperation());

    var textOp = new ConstantOp(new StringAttribute("Hello World!"));
    funcBlock.addOperation(textOp.getOperation());

    var numberTextOP = new ConstantOp(new IntegerAttribute(42));
    funcBlock.addOperation(numberTextOP.getOperation());

    funcBlock.addOperation(new PrintOp(List.of(textOp.getOperation().getOutput(), numberTextOP.getOperation().getOutput())).getOperation());

    String result = mapper.writeValueAsString(op);
    System.out.println(result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    System.out.println(mapper.writeValueAsString(restoredOp));
    assertEquals(mapper.writeValueAsString(op), mapper.writeValueAsString(restoredOp));
  }
}

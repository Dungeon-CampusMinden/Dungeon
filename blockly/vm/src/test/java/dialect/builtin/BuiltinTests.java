package dialect.builtin;

import blockly.vm.dgir.core.ConstantValue;
import blockly.vm.dgir.core.ValueRef;
import blockly.vm.dgir.core.serialization.Utility;
import blockly.vm.dgir.dialect.builtin.types.Int32_t;
import blockly.vm.dgir.dialect.builtin.types.String_t;
import blockly.vm.dgir.dialect.arith.ConstantOp;
import blockly.vm.dgir.dialect.builtin.ProgramOp;
import blockly.vm.dgir.dialect.func.FuncOp;
import blockly.vm.dgir.dialect.io.PrintOp;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

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
    var programRegion = op.region;
    var progBlock = programRegion.getOrCreateDefaultBlock();

    var funcOp = new FuncOp();
    funcOp.ident = "func";
    var funcRegion = funcOp.region;
    var funcBlock = funcRegion.getOrCreateDefaultBlock();
    progBlock.operations.add(funcOp);


    var textOp = new ConstantOp();
    var intConst = new ConstantValue();
    intConst.type = Int32_t.INSTANCE;
    intConst.value = 42;
    textOp.setValue(intConst);
    funcBlock.operations.add(textOp);

    var printOp = new PrintOp();
    var textConst = new ConstantValue();
    textConst.type = String_t.INSTANCE;
    textConst.value = "The answer is: ";
    printOp.inputs.add(textConst);
    var valueRef = new ValueRef();
    valueRef.type = String_t.INSTANCE;
    valueRef.valueIdent = textOp.output.ident;
    printOp.inputs.add(valueRef);
    funcBlock.operations.add(printOp);

    String result = mapper.writeValueAsString(op);
    System.out.println(result);

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    System.out.println(mapper.writeValueAsString(restoredOp));
    assertEquals(mapper.writeValueAsString(op), mapper.writeValueAsString(restoredOp));

  }
}

package dialect.builtin;

import blockly.vm.dgir.core.CFG;
import blockly.vm.dgir.core.Operation;
import blockly.vm.dgir.core.Utils;
import blockly.vm.dgir.core.serialization.Utility;
import blockly.vm.dgir.dialect.builtin.attributes.IntegerAttribute;
import blockly.vm.dgir.dialect.builtin.attributes.StringAttribute;
import blockly.vm.dgir.dialect.arith.ConstantOp;
import blockly.vm.dgir.dialect.builtin.ProgramOp;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;
import blockly.vm.dgir.dialect.builtin.types.StringT;
import blockly.vm.dgir.dialect.func.CallOp;
import blockly.vm.dgir.dialect.func.FuncOp;
import blockly.vm.dgir.dialect.func.ReturnOp;
import blockly.vm.dgir.dialect.func.types.FuncType;
import blockly.vm.dgir.dialect.io.PrintOp;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

    var numberTextOP = new ConstantOp(new IntegerAttribute(42, IntegerT.INT32));
    funcBlock.addOperation(numberTextOP.getOperation());

    funcBlock.addOperation(new PrintOp(List.of(textOp.getOperation().getOutput(), numberTextOP.getOperation().getOutput())).getOperation());

    funcBlock.addOperation(new ReturnOp(List.of()));

    String result = mapper.writeValueAsString(op);
    System.out.println(result);

    try {
      Utils.Graphing.drawGraph(CFG.getCfgFor(null, op.getRegions().getFirst().getBlocks().getFirst()), "simpleProgramCfg.png");
      Utils.Graphing.drawUseGraph(op.getOperation(), "simpleProgramOp.png");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    var restoredOp = mapper.readValue(result, ProgramOp.class);
    System.out.println(mapper.writeValueAsString(restoredOp));
    assertEquals(mapper.writeValueAsString(op), mapper.writeValueAsString(restoredOp));
  }

  @Test
  public void functionCall(){
    ObjectMapper mapper = Utility.getMapper(true, true);


    ProgramOp op = new ProgramOp(true);
    var programRegion = op.getOperation().getRegions().getFirst();
    var progBlock = programRegion.getOrCreateDefaultBlock();

    var fooFuncOp = new FuncOp("foo", new FuncType(List.of(), StringT.INSTANCE));
    {
      var fooFuncRegion = fooFuncOp.getOperation().getFirstRegion();
      var fooFuncBlock = fooFuncRegion.getOrCreateDefaultBlock();

      var helloWorldTextOp = new ConstantOp(new StringAttribute("Hello World!"));
      fooFuncBlock.addOperation(helloWorldTextOp);

      var returnOp = new ReturnOp(List.of(helloWorldTextOp.getOperation().getOutput()));
      fooFuncBlock.addOperation(returnOp);

      progBlock.addOperation(fooFuncOp.getOperation());
    }

    var funcOp = new FuncOp("main");
    {
      var funcRegion = funcOp.getOperation().getRegions().getFirst();
      var funcBlock = funcRegion.getOrCreateDefaultBlock();
      progBlock.addOperation(funcOp.getOperation());

      var funcCallOp = new CallOp(fooFuncOp, List.of());
      funcBlock.addOperation(funcCallOp.getOperation());

      funcBlock.addOperation(new PrintOp(List.of(funcCallOp.getOperation().getOutput())));
      funcBlock.addOperation(new ReturnOp(List.of()));
    }

    String result = mapper.writeValueAsString(op);
    System.out.println(result);

    {
      Operation rawOp = op.getOperation();
      ProgramOp typedOp = rawOp.as(ProgramOp.class);
      assertNotEquals(null, rawOp);
    }

    try {
      Utils.Graphing.drawGraph(CFG.getCfgFor(null, op.getRegions().getFirst().getBlocks().getFirst()), "functionCallCfg.png");
      Utils.Graphing.drawUseGraph(op.getOperation(), "functionCall.png");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

import blockly.vm.dgir.core.DotCFG;
import blockly.vm.dgir.core.serialization.Utils;
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

import static blockly.vm.dgir.core.Utils.Graphing.drawGraph;
import static blockly.vm.dgir.core.Utils.Graphing.drawUseGraph;
import static org.junit.jupiter.api.Assertions.*;

public class BuiltinTests {
  public static boolean printResult = true;
  @Test
  public void emptyProgramOp() {
    ObjectMapper mapper = Utils.getMapper(true, true);

    ProgramOp op = new ProgramOp(true);

    String result = mapper.writeValueAsString(op);
    if (printResult)
      System.out.println(result);

    assertEquals("", TestUtils.compareSerializedOperations(
      mapper,
      op.get(),
      mapper.readValue(result, ProgramOp.class).get()
    ));
  }

  @Test
  public void simpleProgramOp() {
    ObjectMapper mapper = Utils.getMapper(true, true);

    ProgramOp op = new ProgramOp(true);
    var programRegion = op.getOperation().getRegions().getFirst();
    var progBlock = programRegion.getEntryBlock();

    var funcOp = new FuncOp("main");
    var funcRegion = funcOp.getOperation().getRegions().getFirst();
    var funcBlock = funcRegion.getEntryBlock();
    progBlock.addOperation(funcOp.getOperation());

    var textOp = new ConstantOp(new StringAttribute("Hello World!"));
    funcBlock.addOperation(textOp.getOperation());

    var numberTextOP = new ConstantOp(new IntegerAttribute(42, IntegerT.INT32));
    funcBlock.addOperation(numberTextOP.getOperation());

    funcBlock.addOperation(new PrintOp(List.of(textOp.getOutputValue(), numberTextOP.getOutputValue())).getOperation());

    funcBlock.addOperation(new ReturnOp(List.of()));

    String result = mapper.writeValueAsString(op);
    if (printResult)
      System.out.println(result);


    try {
      drawUseGraph(op.getOperation(), "simpleProgramOpUse.png", true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    assertEquals("", TestUtils.compareSerializedOperations(
      mapper,
      op.get(),
      mapper.readValue(result, ProgramOp.class).get()
    ));
  }

  @Test
  public void functionCall() {
    ObjectMapper mapper = Utils.getMapper(true, true);


    ProgramOp op = new ProgramOp(true);
    var programRegion = op.getOperation().getRegions().getFirst();
    var progBlock = programRegion.getEntryBlock();

    var fooFuncOp = new FuncOp("foo", new FuncType(List.of(), StringT.INSTANCE));
    {
      var fooFuncRegion = fooFuncOp.getOperation().getFirstRegion();
      var fooFuncBlock = fooFuncRegion.getEntryBlock();

      var helloWorldTextOp = new ConstantOp(new StringAttribute("Hello World!"));
      fooFuncBlock.addOperation(helloWorldTextOp);

      var returnOp = new ReturnOp(List.of(helloWorldTextOp.getOutputValue()));
      fooFuncBlock.addOperation(returnOp);

      progBlock.addOperation(fooFuncOp.getOperation());
    }

    var funcOp = new FuncOp("main");
    {
      var funcRegion = funcOp.getOperation().getRegions().getFirst();
      var funcBlock = funcRegion.getEntryBlock();
      progBlock.addOperation(funcOp.getOperation());

      var funcCallOp = new CallOp(fooFuncOp, List.of());
      funcBlock.addOperation(funcCallOp.getOperation());

      funcBlock.addOperation(new PrintOp(List.of(funcCallOp.getOperation().getOutput().getValue())));
      funcBlock.addOperation(new ReturnOp(List.of()));
    }

    String result = mapper.writeValueAsString(op);
    if (printResult)
      System.out.println(result);

    try {
      var graph_cluster = DotCFG.buildCfg(op.getOperation());
      drawGraph(graph_cluster.getLeft(), "functionCallCfg.png", true);
      System.out.println(graph_cluster.getRight().toDotString(graph_cluster.getLeft()));
      drawUseGraph(op.getOperation(), "functionCallUse.png", true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    assertEquals("", TestUtils.compareSerializedOperations(
      mapper,
      op.getOperation(),
      mapper.readValue(result, ProgramOp.class).getOperation()
    ));
  }

  /**
   * Checks whether values which are written to multiple types are serialized and deserialized correctly.
   */
  @Test
  public void overrideValue() {
    ObjectMapper mapper = Utils.getMapper(true, true);

    ProgramOp op = new ProgramOp(true);
    var programRegion = op.getOperation().getRegions().getFirst();
    var progBlock = programRegion.getEntryBlock();

    var constOp = new ConstantOp(new IntegerAttribute(42, IntegerT.INT32));
    progBlock.addOperation(constOp.getOperation());

    var secondConstOp = new ConstantOp(new IntegerAttribute(100, IntegerT.INT32));
    secondConstOp.setOutputValue(constOp.getOutputValue());
    progBlock.addOperation(secondConstOp.getOperation());

    progBlock.addOperation(new PrintOp(List.of(secondConstOp.getOutputValue())).getOperation());
    progBlock.addOperation(new ReturnOp(List.of()));

    String result = mapper.writeValueAsString(op);
    if (printResult)
      System.out.println(result);

    assertEquals("", TestUtils.compareSerializedOperations(
      mapper,
      op.getOperation(),
      mapper.readValue(result, ProgramOp.class).getOperation()
    ));
  }
}

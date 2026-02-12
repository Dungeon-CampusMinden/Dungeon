import blockly.vm.dgir.core.Dialect;
import blockly.vm.dgir.core.analysis.DotCFG;
import blockly.vm.dgir.core.serialization.Utils;
import blockly.vm.dgir.dialect.arith.ConstantOp;
import blockly.vm.dgir.dialect.builtin.ProgramOp;
import blockly.vm.dgir.dialect.builtin.types.StringT;
import blockly.vm.dgir.dialect.func.CallOp;
import blockly.vm.dgir.dialect.func.FuncOp;
import blockly.vm.dgir.dialect.func.ReturnOp;
import blockly.vm.dgir.dialect.func.types.FuncType;
import blockly.vm.dgir.dialect.io.PrintOp;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static blockly.vm.dgir.core.Utils.Graphing.drawGraph;
import static blockly.vm.dgir.core.Utils.Graphing.drawUseGraph;
import static org.junit.jupiter.api.Assertions.*;

public class BuiltinTests {
  public static boolean printResult = true;

  static ObjectMapper mapper;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
    mapper = Utils.getMapper(true);
  }

  @Test
  public void emptyProgramOp() {
    ProgramOp op = new ProgramOp();

    String result = mapper.writeValueAsString(op);
    if (printResult)
      System.out.println(result);

    assertFalse(op.verify(true));

    assertEquals("", TestUtils.compareSerializedOperations(
      mapper,
      op.get(),
      result
    ));
  }

  @Test
  public void simpleProgramOp() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var textOp = funcMainOp.addOperation(new ConstantOp("Hello World!"));
    var numberTextOP = funcMainOp.addOperation(new ConstantOp(42));

    funcMainOp.addOperation(new PrintOp(textOp.getOutputValue(), numberTextOP.getOutputValue()));
    funcMainOp.addOperation(new ReturnOp());

    String result = mapper.writeValueAsString(programOp);
    if (printResult)
      System.out.println(result);

    assertTrue(programOp.verify(true));

    try {
      drawUseGraph(programOp.getOperation(), "simpleProgramOpUse.png", true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    assertEquals("", TestUtils.compareSerializedOperations(
      mapper,
      programOp.get(),
      result
    ));
  }

  @Test
  public void functionCall() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var fooFuncOp = programOp.addOperation(
      new FuncOp(
        "foo",
        new FuncType(List.of(StringT.INSTANCE), StringT.INSTANCE)));
    {
      fooFuncOp.addOperation(new ReturnOp(fooFuncOp.getArgument(0)));
    }

    {
      var helloWorldTextOp = funcMainOp.addOperation(new ConstantOp("Hello World!"));
      var funcCallOp = funcMainOp.addOperation(new CallOp(fooFuncOp, helloWorldTextOp.getOutputValue()));
      funcMainOp.addOperation(new PrintOp(funcCallOp.getOutputValue()));
      funcMainOp.addOperation(new ReturnOp());
    }

    String result = mapper.writeValueAsString(programOp);
    if (printResult)
      System.out.println(result);

    assertTrue(programOp.verify(true));

    try {
      var graph_cluster = DotCFG.buildCfg(programOp.getOperation());
      drawGraph(graph_cluster.getLeft(), "functionCallCfg.png", true);
      System.out.println(graph_cluster.getRight().toDotString(graph_cluster.getLeft()));
      drawUseGraph(programOp.getOperation(), "functionCallUse.png", true);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    assertEquals("", TestUtils.compareSerializedOperations(
      mapper,
      programOp.getOperation(),
      result
    ));
  }

  /**
   * Checks whether values which are written to multiple types are serialized and deserialized correctly.
   */
  @Test
  public void overrideValue() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var constOp = funcMainOp.addOperation(new ConstantOp(42));

    var secondConstOp = funcMainOp.addOperation(
      new ConstantOp(100)
        .setOutputValue(constOp.getOutputValue())
    );

    funcMainOp.addOperation(new PrintOp(secondConstOp.getOutputValue()));
    funcMainOp.addOperation(new ReturnOp());

    String result = mapper.writeValueAsString(programOp);
    if (printResult)
      System.out.println(result);

    assertTrue(programOp.verify(true));

    assertEquals("", TestUtils.compareSerializedOperations(
      mapper,
      programOp.getOperation(),
      result
    ));
  }

  /**
   * Checks whether an incorrect program with a function without terminator is correctly rejected by the verifier.
   */
  @Test
  public void missingTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();

    if (printResult)
      System.out.println(mapper.writeValueAsString(programOp));

    assertFalse(programOp.verify(true));
  }
}

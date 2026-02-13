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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BuiltinTests {
  public static boolean printResult = true;
  public static boolean printDotGraph = false;

  static ObjectMapper mapper;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
    mapper = Utils.getMapper(true);
  }

  @Test
  public void emptyProgramOp() {
    ProgramOp programOp = new ProgramOp();

    assertFalse(programOp.verify(true));

    TestUtils.testSerialization(mapper, programOp, printResult, printDotGraph);
  }

  @Test
  public void simpleProgramOp() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var textOp = funcMainOp.addOperation(new ConstantOp("Hello World!"), 0);
    var numberTextOP = funcMainOp.addOperation(new ConstantOp(42), 0);

    funcMainOp.addOperation(new PrintOp(textOp.getOutputValue(), numberTextOP.getOutputValue()), 0);
    funcMainOp.addOperation(new ReturnOp(), 0);

    assertTrue(programOp.verify(true));

    TestUtils.testSerialization(mapper, programOp, printResult, printDotGraph);
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
      fooFuncOp.addOperation(new ReturnOp(fooFuncOp.getArgument(0)), 0);
    }

    {
      var helloWorldTextOp = funcMainOp.addOperation(new ConstantOp("Hello World!"), 0);
      var funcCallOp = funcMainOp.addOperation(new CallOp(fooFuncOp, helloWorldTextOp.getOutputValue()), 0);
      funcMainOp.addOperation(new PrintOp(funcCallOp.getOutputValue()), 0);
      funcMainOp.addOperation(new ReturnOp(), 0);
    }

    assertTrue(programOp.verify(true));

    TestUtils.testSerialization(mapper, programOp, printResult, printDotGraph);
  }

  /**
   * Checks whether values which are written to multiple types are serialized and deserialized correctly.
   */
  @Test
  public void overrideValue() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var constOp = funcMainOp.addOperation(new ConstantOp(42), 0);

    var secondConstOp = funcMainOp.addOperation(
      new ConstantOp(100)
        .setOutputValue(constOp.getOutputValue())
      , 0
    );

    funcMainOp.addOperation(new PrintOp(secondConstOp.getOutputValue()), 0);
    funcMainOp.addOperation(new ReturnOp(), 0);

    assertTrue(programOp.verify(true));

    TestUtils.testSerialization(mapper, programOp, printResult, printDotGraph);
  }

  /**
   * Checks whether an incorrect program with a function without terminator is correctly rejected by the verifier.
   */
  @Test
  public void missingTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();

    assertFalse(programOp.verify(true));

    TestUtils.testSerialization(mapper, programOp, printResult, printDotGraph);
  }
}

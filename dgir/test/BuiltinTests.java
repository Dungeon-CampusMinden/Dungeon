import static org.junit.jupiter.api.Assertions.*;

import core.Dialect;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.builtin.types.StringT;
import dialect.func.CallOp;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.func.types.FuncType;
import dialect.io.PrintOp;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BuiltinTests {
  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
  }

  @Test
  public void emptyProgramOp() {
    ProgramOp programOp = new ProgramOp();

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void simpleProgramOp() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var textOp = funcMainOp.addOperation(new ConstantOp("Hello World!"), 0);
    var numberTextOP = funcMainOp.addOperation(new ConstantOp(42), 0);

    funcMainOp.addOperation(
        new PrintOp(textOp.getOutputValueThrowing(), numberTextOP.getOutputValueThrowing()), 0);
    funcMainOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void functionCall() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var fooFuncOp =
        programOp.addOperation(
            new FuncOp("foo", new FuncType(List.of(StringT.INSTANCE), StringT.INSTANCE)));
    {
      fooFuncOp.addOperation(new ReturnOp(fooFuncOp.getArgument(0).orElseThrow()), 0);
    }

    {
      var helloWorldTextOp = funcMainOp.addOperation(new ConstantOp("Hello World!"), 0);
      var funcCallOp =
          funcMainOp.addOperation(
              new CallOp(fooFuncOp, helloWorldTextOp.getOutputValueThrowing()), 0);
      funcMainOp.addOperation(new PrintOp(funcCallOp.getOutputValueThrowing()), 0);
      funcMainOp.addOperation(new ReturnOp(), 0);
    }

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  /**
   * Checks whether values which are written to multiple types are serialized and deserialized
   * correctly.
   */
  @Test
  public void overrideValue() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var constOp = funcMainOp.addOperation(new ConstantOp(42), 0);

    var secondConstOp =
        funcMainOp.addOperation(new ConstantOp(100).setOutputValue(constOp.getValue()), 0);

    funcMainOp.addOperation(new PrintOp(secondConstOp.getOutputValueThrowing()), 0);
    funcMainOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  /**
   * Checks whether an incorrect program with a function without terminator is correctly rejected by
   * the verifier.
   */
  @Test
  public void missingTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }
}

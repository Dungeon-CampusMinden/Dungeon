import core.Dialect;
import core.debug.Location;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.builtin.types.StringT;
import dialect.func.CallOp;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.func.types.FuncType;
import dialect.io.PrintOp;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuiltinTests {
  private static final Location LOC = Location.UNKNOWN;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
  }

  @Test
  public void emptyProgramOp() {
    ProgramOp programOp = new ProgramOp(LOC);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void simpleProgramOp() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var textOp = funcMainOp.addOperation(new ConstantOp(LOC, "Hello World!"), 0);
    var numberTextOP = funcMainOp.addOperation(new ConstantOp(LOC, 42), 0);

    funcMainOp.addOperation(
        new PrintOp(LOC, textOp.getResult(), numberTextOP.getResult()), 0);
    funcMainOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void functionCall() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMainOp = entry.getRight();

    var fooFuncOp =
        programOp.addOperation(
            new FuncOp(LOC, "foo", new FuncType(List.of(StringT.INSTANCE), StringT.INSTANCE)));
    {
      fooFuncOp.addOperation(new ReturnOp(LOC, fooFuncOp.getArgument(0).orElseThrow()), 0);
    }

    {
      var helloWorldTextOp = funcMainOp.addOperation(new ConstantOp(LOC, "Hello World!"), 0);
      var funcCallOp =
          funcMainOp.addOperation(
              new CallOp(LOC, fooFuncOp, helloWorldTextOp.getResult()), 0);
      funcMainOp.addOperation(new PrintOp(LOC, funcCallOp.getOutputValue().orElseThrow()), 0);
      funcMainOp.addOperation(new ReturnOp(LOC), 0);
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

    var constOp = funcMainOp.addOperation(new ConstantOp(LOC, 42), 0);

    var secondConstOp =
        funcMainOp.addOperation(new ConstantOp(LOC, 100).setOutputValue(constOp.getValue()), 0);

    funcMainOp.addOperation(new PrintOp(LOC, secondConstOp.getOutputValue().orElseThrow()), 0);
    funcMainOp.addOperation(new ReturnOp(LOC), 0);

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

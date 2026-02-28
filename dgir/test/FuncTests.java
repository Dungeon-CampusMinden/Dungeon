import core.Dialect;
import core.debug.Location;
import core.serialization.Utils;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.builtin.types.IntegerT;
import dialect.builtin.types.StringT;
import dialect.func.CallOp;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.func.types.FuncType;
import dialect.io.PrintOp;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for FuncOp and related operations. These test check for correct serialization and
 * deserialization of FuncOp, as well as correct verification of the operation and its operands.
 * They also check for correct handling of function parameters and return values.
 *
 * <p>There are multiple positive and negative test cases for each operation.
 */
public class FuncTests {
  static final Location LOC = Location.UNKNOWN;
  public static boolean printResult = true;
  public static boolean printDotGraph = false;
  static ObjectMapper mapper;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
    mapper = Utils.getMapper(true);
  }

  /**
   * Basic test case to check for correct serialization and deserialization of a simple FuncOp with
   * no parameters and no return value.
   */
  @Test
  public void basicFuncSerialization() {
    FuncOp funcOp = new FuncOp(LOC, "testFunc");
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(funcOp));
  }

  /** Test case for a function with multiple parameters and a return value. */
  @Test
  public void funcWithParamsAndReturn() {
    FuncType type = new FuncType(List.of(IntegerT.INT32, IntegerT.INT32), IntegerT.INT32);
    FuncOp funcOp = new FuncOp(LOC, "add", type);

    // In a real scenario, we might have an add operation here. For this test, we just return one of
    // the parameters.
    funcOp.addOperation(new ReturnOp(LOC, funcOp.getArgument(0).orElseThrow()), 0);

    assertTrue(TestUtils.testValidityAndSerialization(funcOp));
  }

  /**
   * Test case that checks if a function with an incorrect return type is rejected by the verifier.
   */
  @Test
  public void funcWithMismatchedReturn() {
    FuncType type = new FuncType(List.of(IntegerT.INT32), StringT.INSTANCE);
    FuncOp funcOp = new FuncOp(LOC, "mismatch", type);

    // Returning an INT32 when the function expects StringT
    funcOp.addOperation(new ReturnOp(LOC, funcOp.getArgument(0).orElseThrow()), 0);

    assertFalse(TestUtils.testValidityAndSerialization(funcOp));
  }

  /** Test case for a recursive function call. */
  @Test
  public void recursiveFuncCall() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    FuncType type = new FuncType(List.of(IntegerT.INT32), IntegerT.INT32);
    FuncOp factorial = programOp.addOperation(new FuncOp(LOC, "factorial", type));

    // Simple recursive call without base case for IR structure testing
    var callOp = factorial.addOperation(new CallOp(LOC, factorial, factorial.getArgument(0).orElseThrow()), 0);
    factorial.addOperation(new ReturnOp(LOC, callOp.getOutputValue().orElseThrow()), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void callBetweenFunctions() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    FuncOp otherFunc =
        programOp.addOperation(new FuncOp(LOC, "other", new FuncType(List.of(), IntegerT.INT32)));
    var constOp = otherFunc.addOperation(new ConstantOp(LOC, 42), 0);
    otherFunc.addOperation(new ReturnOp(LOC, constOp.getValue()), 0);

    var callOp = mainFunc.addOperation(new CallOp(LOC, otherFunc), 0);
    mainFunc.addOperation(new PrintOp(LOC, callOp.getOutputValue().orElseThrow()), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void callToNonExistentFunction() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    mainFunc.addOperation(new CallOp(LOC, "ghost", new FuncType(List.of(), IntegerT.INT32)), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void invalidArgumentCountCall() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    FuncOp target =
        programOp.addOperation(
            new FuncOp(LOC, "target", new FuncType(List.of(IntegerT.INT32), IntegerT.INT32)));
    target.addOperation(new ReturnOp(LOC, target.getArgument(0).orElseThrow()), 0);

    // Call with 0 args, expects 1
    mainFunc.addOperation(new CallOp(LOC, target), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void invalidArgumentTypeCall() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    FuncOp target =
        programOp.addOperation(
            new FuncOp(LOC, "target", new FuncType(List.of(IntegerT.INT32), IntegerT.INT32)));
    target.addOperation(new ReturnOp(LOC, target.getArgument(0).orElseThrow()), 0);

    // Call with String arg, expects Int
    var strOp = mainFunc.addOperation(new ConstantOp(LOC, "test"), 0);
    mainFunc.addOperation(new CallOp(LOC, target, strOp.getValue()), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }
}

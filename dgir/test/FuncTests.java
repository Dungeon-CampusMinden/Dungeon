import dgir.core.Dialect;
import dgir.core.debug.Location;
import dgir.core.serialization.Utils;
import dgir.dialect.str.StrTypes;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.builtin.BuiltinTypes.IntegerT;
import static dgir.dialect.func.FuncOps.*;
import static dgir.dialect.func.FuncTypes.FuncType;
import static dgir.dialect.io.IoOps.PrintOp;
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
    FuncType type = FuncType.of(List.of(IntegerT.INT32, IntegerT.INT32), IntegerT.INT32);
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
    FuncType type = FuncType.of(List.of(IntegerT.INT32), StrTypes.StringT.INSTANCE);
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

    FuncType type = FuncType.of(List.of(IntegerT.INT32), IntegerT.INT32);
    FuncOp factorial = programOp.addOperation(new FuncOp(LOC, "factorial", type));

    // Simple recursive call without base case for IR structure testing
    var callOp =
        factorial.addOperation(
            new CallOp(LOC, factorial, factorial.getArgument(0).orElseThrow()), 0);
    factorial.addOperation(new ReturnOp(LOC, callOp.getOutputValue().orElseThrow()), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void callBetweenFunctions() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    FuncOp otherFunc =
        programOp.addOperation(new FuncOp(LOC, "other", FuncType.of(List.of(), IntegerT.INT32)));
    var constOp = otherFunc.addOperation(new ConstantOp(LOC, 42), 0);
    otherFunc.addOperation(new ReturnOp(LOC, constOp.getResult()), 0);

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

    mainFunc.addOperation(new CallOp(LOC, "ghost", FuncType.of(List.of(), IntegerT.INT32)), 0);
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
            new FuncOp(LOC, "target", FuncType.of(List.of(IntegerT.INT32), IntegerT.INT32)));
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
            new FuncOp(LOC, "target", FuncType.of(List.of(IntegerT.INT32), IntegerT.INT32)));
    target.addOperation(new ReturnOp(LOC, target.getArgument(0).orElseThrow()), 0);

    // Call with String arg, expects Int
    var strOp = mainFunc.addOperation(new ConstantOp(LOC, "test"), 0);
    mainFunc.addOperation(new CallOp(LOC, target, strOp.getResult()), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }
}

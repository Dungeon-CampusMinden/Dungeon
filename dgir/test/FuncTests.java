import dgir.core.Dialect;
import dgir.core.debug.Location;
import dgir.core.serialization.Utils;
import dgir.dialect.arith.ArithOps;
import dgir.dialect.str.StrTypes;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.builtin.BuiltinTypes.IntegerT;
import static dgir.dialect.func.FuncOps.*;
import static dgir.dialect.func.FuncTypes.FuncType;
import static dgir.dialect.io.IoOps.PrintOp;
import static org.junit.jupiter.api.Assertions.*;

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
    var constOp = otherFunc.addOperation(new ArithOps.ConstantOp(LOC, 42), 0);
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
    var strOp = mainFunc.addOperation(new ArithOps.ConstantOp(LOC, "test"), 0);
    mainFunc.addOperation(new CallOp(LOC, target, strOp.getResult()), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  // =========================================================================
  // ConstantOp tests
  // =========================================================================

  /**
   * Basic test for a {@code func.constant} that holds a reference to an existing function. The
   * result type must be the referenced function's {@link FuncType}.
   */
  @Test
  public void basicConstantFuncRef() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    FuncType targetType = FuncType.of(List.of(IntegerT.INT32), IntegerT.INT32);
    FuncOp targetFunc = programOp.addOperation(new FuncOp(LOC, "target", targetType));
    targetFunc.addOperation(new ReturnOp(LOC, targetFunc.getArgument(0).orElseThrow()), 0);

    // Create a constant reference to the target function; its result type must be a FuncType.
    var funcRef = mainFunc.addOperation(new ConstantOp(LOC, targetFunc), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertInstanceOf(FuncType.class, funcRef.getResult().getType());
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  /**
   * Test that a {@code func.constant} can be serialized and deserialized when constructed from a
   * name and explicit {@link FuncType} (without a live {@link FuncOp} reference).
   */
  @Test
  public void constantFuncRefByName() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    FuncType targetType = FuncType.of(List.of(), IntegerT.INT32);
    FuncOp targetFunc = programOp.addOperation(new FuncOp(LOC, "noArgFunc", targetType));
    var constOp = targetFunc.addOperation(new ArithOps.ConstantOp(LOC, 0), 0);
    targetFunc.addOperation(new ReturnOp(LOC, constOp.getResult()), 0);

    // Construct via explicit name + type overload
    mainFunc.addOperation(new ConstantOp(LOC, "noArgFunc", targetType), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  // =========================================================================
  // CallIndirectOp tests
  // =========================================================================

  /**
   * Positive test: indirect call to a no-argument function through a {@code func.constant}
   * reference.
   */
  @Test
  public void callIndirectBasic() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    // target: () -> int32
    FuncType targetType = FuncType.of(List.of(), IntegerT.INT32);
    FuncOp targetFunc = programOp.addOperation(new FuncOp(LOC, "target", targetType));
    var fortyTwo = targetFunc.addOperation(new ArithOps.ConstantOp(LOC, 42), 0);
    targetFunc.addOperation(new ReturnOp(LOC, fortyTwo.getResult()), 0);

    // Obtain a function reference and call it indirectly.
    var funcRef = mainFunc.addOperation(new ConstantOp(LOC, targetFunc), 0);
    var callOp = mainFunc.addOperation(new CallIndirectOp(LOC, funcRef.getResult(), List.of()), 0);
    mainFunc.addOperation(new PrintOp(LOC, callOp.getOutputValue().orElseThrow()), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  /**
   * Positive test: indirect call to a function that takes arguments, with correctly typed
   * arguments.
   */
  @Test
  public void callIndirectWithArgs() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    // identity: (int32) -> int32
    FuncType targetType = FuncType.of(List.of(IntegerT.INT32), IntegerT.INT32);
    FuncOp identityFunc = programOp.addOperation(new FuncOp(LOC, "identity", targetType));
    identityFunc.addOperation(new ReturnOp(LOC, identityFunc.getArgument(0).orElseThrow()), 0);

    var funcRef = mainFunc.addOperation(new ConstantOp(LOC, identityFunc), 0);
    var arg = mainFunc.addOperation(new ArithOps.ConstantOp(LOC, 7), 0);
    var callOp =
        mainFunc.addOperation(
            new CallIndirectOp(LOC, funcRef.getResult(), List.of(arg.getResult())), 0);
    mainFunc.addOperation(new PrintOp(LOC, callOp.getOutputValue().orElseThrow()), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  /**
   * Negative test: indirect call where the argument count does not match the function's expected
   * parameter count.
   */
  @Test
  public void callIndirectWrongArgCount() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    // target expects one INT32 argument
    FuncType targetType = FuncType.of(List.of(IntegerT.INT32), IntegerT.INT32);
    FuncOp targetFunc = programOp.addOperation(new FuncOp(LOC, "target", targetType));
    targetFunc.addOperation(new ReturnOp(LOC, targetFunc.getArgument(0).orElseThrow()), 0);

    // Call with zero arguments — signature mismatch
    var funcRef = mainFunc.addOperation(new ConstantOp(LOC, targetFunc), 0);
    mainFunc.addOperation(new CallIndirectOp(LOC, funcRef.getResult(), List.of()), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  /**
   * Negative test: indirect call where the argument type does not match the function's expected
   * parameter type.
   */
  @Test
  public void callIndirectWrongArgType() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    // target expects INT32, but we pass a String
    FuncType targetType = FuncType.of(List.of(IntegerT.INT32), IntegerT.INT32);
    FuncOp targetFunc = programOp.addOperation(new FuncOp(LOC, "target", targetType));
    targetFunc.addOperation(new ReturnOp(LOC, targetFunc.getArgument(0).orElseThrow()), 0);

    var funcRef = mainFunc.addOperation(new ConstantOp(LOC, targetFunc), 0);
    var strArg = mainFunc.addOperation(new ArithOps.ConstantOp(LOC, "notAnInt"), 0);
    mainFunc.addOperation(
        new CallIndirectOp(LOC, funcRef.getResult(), List.of(strArg.getResult())), 0);
    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }
}

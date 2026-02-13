import blockly.vm.dgir.core.Dialect;
import blockly.vm.dgir.core.serialization.Utils;
import blockly.vm.dgir.dialect.arith.ConstantOp;
import blockly.vm.dgir.dialect.builtin.ProgramOp;
import blockly.vm.dgir.dialect.builtin.types.IntegerT;
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

/**
 * Test cases for FuncOp and related operations. These test check for correct serialization and deserialization of FuncOp,
 * as well as correct verification of the operation and its operands. They also check for correct handling of function
 * parameters and return values.
 *
 * There are multiple positive and negative test cases for each operation.
 *
 */
public class FuncTests {
  public static boolean printResult = true;
  public static boolean printDotGraph = false;
  static ObjectMapper mapper;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
    mapper = Utils.getMapper(true);
  }

  /**
   * Basic test case to check for correct serialization and deserialization of a simple FuncOp with no parameters and no return value.
   */
  @Test
  public void basicFuncSerialization() {
    FuncOp funcOp = new FuncOp("testFunc");
    funcOp.addOperation(new ReturnOp(), 0);

    TestUtils.testSerialization(mapper, funcOp, printResult, printDotGraph);
    assertTrue(funcOp.verify(true));
  }

  /**
   * Test case for a function with multiple parameters and a return value.
   */
  @Test
  public void funcWithParamsAndReturn() {
    FuncType type = new FuncType(List.of(IntegerT.INT32, IntegerT.INT32), IntegerT.INT32);
    FuncOp funcOp = new FuncOp("add", type);

    // In a real scenario, we might have an add operation here. For this test, we just return one of the parameters.
    funcOp.addOperation(new ReturnOp(funcOp.getArgument(0)), 0);

    TestUtils.testSerialization(mapper, funcOp, printResult, printDotGraph);
    assertTrue(funcOp.verify(true));
  }

  /**
   * Test case that checks if a function with an incorrect return type is rejected by the verifier.
   */
  @Test
  public void funcWithMismatchedReturn() {
    FuncType type = new FuncType(List.of(IntegerT.INT32), StringT.INSTANCE);
    FuncOp funcOp = new FuncOp("mismatch", type);

    // Returning an INT32 when the function expects StringT
    funcOp.addOperation(new ReturnOp(funcOp.getArgument(0)), 0);

    TestUtils.testSerialization(mapper, funcOp, printResult, printDotGraph);
    assertFalse(funcOp.verify(true));
  }

  /**
   * Test case for a recursive function call.
   */
  @Test
  public void recursiveFuncCall() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();
    mainFunc.addOperation(new ReturnOp(), 0);

    FuncType type = new FuncType(List.of(IntegerT.INT32), IntegerT.INT32);
    FuncOp factorial = programOp.addOperation(new FuncOp("factorial", type));

    // Simple recursive call without base case for IR structure testing
    var callOp = factorial.addOperation(new CallOp(factorial, factorial.getArgument(0)), 0);
    factorial.addOperation(new ReturnOp(callOp.getOutputValue()), 0);

    TestUtils.testSerialization(mapper, programOp, printResult, printDotGraph);
    assertTrue(programOp.verify(true));
  }

  @Test
  public void callBetweenFunctions() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    FuncOp otherFunc = programOp.addOperation(new FuncOp("other", new FuncType(List.of(), IntegerT.INT32)));
    var constOp = otherFunc.addOperation(new ConstantOp(42), 0);
    otherFunc.addOperation(new ReturnOp(constOp.getOutputValue()), 0);

    var callOp = mainFunc.addOperation(new CallOp(otherFunc), 0);
    mainFunc.addOperation(new PrintOp(callOp.getOutputValue()), 0);
    mainFunc.addOperation(new ReturnOp(), 0);

    TestUtils.testSerialization(mapper, programOp, printResult, printDotGraph);
    assertTrue(programOp.verify(true));
  }

  @Test
  public void callToNonExistentFunction() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    mainFunc.addOperation(new CallOp("ghost", new FuncType(List.of(), IntegerT.INT32)), 0);
    mainFunc.addOperation(new ReturnOp(), 0);

    TestUtils.testSerialization(mapper, programOp, printResult, printDotGraph);
    assertFalse(programOp.verify(true));
  }

  @Test
  public void invalidArgumentCountCall() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    FuncOp target = programOp.addOperation(new FuncOp("target", new FuncType(List.of(IntegerT.INT32), IntegerT.INT32)));
    target.addOperation(new ReturnOp(target.getArgument(0)), 0);

    // Call with 0 args, expects 1
    mainFunc.addOperation(new CallOp(target), 0);
    mainFunc.addOperation(new ReturnOp(), 0);

    TestUtils.testSerialization(mapper, programOp, printResult, printDotGraph);
    assertFalse(programOp.verify(true));
  }

  @Test
  public void invalidArgumentTypeCall() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    FuncOp target = programOp.addOperation(new FuncOp("target", new FuncType(List.of(IntegerT.INT32), IntegerT.INT32)));
    target.addOperation(new ReturnOp(target.getArgument(0)), 0);

    // Call with String arg, expects Int
    var strOp = mainFunc.addOperation(new ConstantOp("test"), 0);
    mainFunc.addOperation(new CallOp(target, strOp.getOutputValue()), 0);
    mainFunc.addOperation(new ReturnOp(), 0);

    TestUtils.testSerialization(mapper, programOp, printResult, printDotGraph);
    assertFalse(programOp.verify(true));
  }
}

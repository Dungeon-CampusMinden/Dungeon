import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;

/** Test the server class. */
public class TestServer {
  private Server server;

  /** Reset all global values before each test. */
  @BeforeEach
  public void setUp() throws IOException {
    server = Server.instance();
    server.clearGlobalValues();
  }

  /** Test a simple variable assign. */
  @Test
  public void testSimpleAssign() {}

  /** Test variable assign with expressions. */
  @Test
  public void testExpressionAssign() {}

  /** Test evaluation of conditions. */
  @Test
  public void testConditionEval() {}

  /** Test if if-statements will be recognized correctly and if/else flag will be set properly. */
  @Test
  public void testIfEval() {}

  /** Test nested if-statements. */
  @Test
  public void testNestedIf() {}

  /** Test nested if-statements with a negative condition at the top-level. */
  @Test
  public void testNestedIfNegativeOuterCondition() {}

  /**
   * Test if while-loops will be recognized correctly and execution of the while-body works
   * properly.
   */
  @Test
  public void testWhileEval() {}

  /** Test while-loop with a condition that evaluates to false. */
  @Test
  public void testWhileFalseCondition() {}

  /** Test nested while-loops. */
  @Test
  public void testNestedWhile() {}

  /** Test nested whiles with a while-loop that evaluates to false at the top-level. */
  @Test
  public void testNestedWhileNegativeOuterCondition() {}

  /**
   * Test if repeat-loops will be recognized correctly and execution of the repeat-body works
   * properly.
   */
  @Test
  public void testRepeatEval() {}

  /** Test nested repeat loops. */
  @Test
  public void testNestedRepeat() {}

  /** Test nested scopes of different types. */
  @Test
  public void testNestedScopes() {}

  /** Test if func defs will be recognized correctly and the code will not actually be executed. */
  @Test
  public void testFuncEval() {}

  /**
   * Test if func calls will be recognized correctly and the func body will be executed properly.
   */
  @Test
  public void testFuncCall() {}

  /** Test func calls in func def. The code of the func call must not be executed. */
  @Test
  public void testFuncCallInFuncDef() {}

  /**
   * Test if array creation will be recognized correctly and array variable will be created
   * properly.
   */
  @Test
  public void testArrayCreation() {}

  /** Test if a value can be retrieved from an array properly. */
  @Test
  public void testArrayGet() {}

  /** Test if a value can be set in an array properly. */
  @Test
  public void testArraySet() {}

  /** Test if the length of an array can be retrieved properly. */
  @Test
  public void testArrayLength() {}

  /** Test if the variable not found error works properly. */
  @Test
  public void testVariableNotFoundError() {}

  /** Test if the array is an array but a base variable was expected error works properly. */
  @Test
  public void testVariableIsArrayError() {}

  /** Test if an index out of bounds exception will be caught properly. */
  @Test
  public void testIndexOutOfBoundsError() {}

  /**
   * Test if an error will be raised properly if an array variable was expected, but we got a base
   * variable.
   */
  @Test
  public void testVariableIsBaseError() {}

  /** Test if we properly raise an error if we do not find a function. */
  @Test
  public void testFunctionNotFoundError() {}

  /** Test if we properly raise an error when we can not find a variable in a condition. */
  @Test
  public void testConditionError() {}

  /**
   * Test if we properly raise an error when we got a variable with the wrong type in a condition.
   */
  @Test
  public void testConditionWrongVarType() {}

  /** Test if we properly catch a division by zero and raise an error. */
  @Test
  public void testDivisionByZeroError() {}
}

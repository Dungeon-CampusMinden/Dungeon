import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Server;

/** Test the server class. */
public class TestServer {

  /** Reset all global values before each test. */
  @BeforeEach
  public void setUp() {
    Server.clearGlobalValues();
  }

  /** Test a simple variable assign. */
  @Test
  public void testSimpleAssign() {
    Server.processAction("int x = 5;");
    assertEquals(5, Server.variables.get("x").intVal);
  }

  /** Test variable assign with expressions. */
  @Test
  public void testExpressionAssign() {
    Server.processAction("int x = 5 + 5;");
    assertEquals(10, Server.variables.get("x").intVal);
    Server.processAction("int x = 5 * 5;");
    assertEquals(25, Server.variables.get("x").intVal);
    Server.processAction("int x = 5 - 5;");
    assertEquals(0, Server.variables.get("x").intVal);
    Server.processAction("int x = 5 / 5;");
    assertEquals(1, Server.variables.get("x").intVal);
    Server.processAction("int x = x + x;");
    assertEquals(2, Server.variables.get("x").intVal);
    Server.processAction("int x = x + 3;");
    assertEquals(5, Server.variables.get("x").intVal);
    Server.processAction("int x = 5 - x;");
    assertEquals(0, Server.variables.get("x").intVal);
  }

  /** Test evaluation of conditions. */
  @Test
  public void testConditionEval() {
    Pattern pattern = Pattern.compile("(.*)");
    // Check for simple booleans
    assertFalse(Server.evalComplexCondition("falsch", pattern));
    assertTrue(Server.evalComplexCondition("wahr", pattern));
    assertTrue(Server.evalComplexCondition("nicht falsch", pattern));
    assertFalse(Server.evalComplexCondition("nicht wahr", pattern));
    // Compare operator
    assertTrue(Server.evalComplexCondition("5 < 10", pattern));
    assertFalse(Server.evalComplexCondition("5 > 10", pattern));
    assertFalse(Server.evalComplexCondition("5 == 10", pattern));
    assertTrue(Server.evalComplexCondition("5 != 10", pattern));
    // Edge cases
    assertFalse(Server.evalComplexCondition("11 <= 10", pattern));
    assertTrue(Server.evalComplexCondition("10 <= 10", pattern));
    assertTrue(Server.evalComplexCondition("9 <= 10", pattern));
    assertTrue(Server.evalComplexCondition("10 >= 10", pattern));
    assertTrue(Server.evalComplexCondition("11 >= 10", pattern));
    assertFalse(Server.evalComplexCondition("9 >= 10", pattern));
    // Logic operator
    assertTrue(Server.evalComplexCondition("9 <= 10 && wahr", pattern));
    assertFalse(Server.evalComplexCondition("nicht (9 <= 10 && wahr)", pattern));
    assertTrue(Server.evalComplexCondition("9 >= 10 || wahr", pattern));
    // Complex condition
    assertTrue(
        Server.evalComplexCondition(
            "(((nicht falsch && 10 >= 1) || (11 <= 10 && falsch)))", pattern));
  }

  /** Test if if-statements will be recognized correctly and if/else flag will be set properly. */
  @Test
  public void testIfEval() {
    Server.processAction("falls (wahr)");
    assertTrue(Server.active_ifs.peek().if_flag);
    assertFalse(Server.active_ifs.peek().else_flag);
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("} sonst {");
    assertFalse(Server.active_ifs.peek().if_flag);
    assertFalse(Server.active_ifs.peek().else_flag);
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("}");
    assertTrue(Server.active_ifs.isEmpty());
    assertTrue(Server.active_scopes.isEmpty());
  }

  /** Test nested if-statements. */
  @Test
  public void testNestedIf() {
    Server.processAction("falls (wahr)");
    assertTrue(Server.active_ifs.peek().if_flag);
    assertFalse(Server.active_ifs.peek().else_flag);
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("falls (falsch)");
    assertFalse(Server.active_ifs.peek().if_flag);
    assertFalse(Server.active_ifs.peek().else_flag);
    assertEquals(2, Server.active_ifs.size());
    assertEquals("if", Server.active_scopes.peek());
    assertEquals(2, Server.active_scopes.size());

    Server.processAction("}");
    assertTrue(Server.active_ifs.peek().if_flag);
    assertFalse(Server.active_ifs.peek().else_flag);
    assertEquals(1, Server.active_ifs.size());
    assertEquals("if", Server.active_scopes.peek());
    assertEquals(1, Server.active_scopes.size());

    Server.processAction("} sonst {");
    assertFalse(Server.active_ifs.peek().if_flag);
    assertFalse(Server.active_ifs.peek().else_flag);
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("}");
    assertTrue(Server.active_ifs.isEmpty());
    assertTrue(Server.active_scopes.isEmpty());
  }

  /** Test nested if-statements with a negative condition at the top-level. */
  @Test
  public void testNestedIfNegativeOuterCondition() {
    Server.processAction("int x = 0;");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("falls (falsch)");
    assertFalse(Server.active_ifs.peek().if_flag);
    assertFalse(Server.active_ifs.peek().else_flag);
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("falls (wahr)");
    assertTrue(Server.active_ifs.peek().if_flag);
    assertFalse(Server.active_ifs.peek().else_flag);

    Server.processAction("int x = x + 1");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("}");
    assertFalse(Server.active_ifs.peek().if_flag);
    assertFalse(Server.active_ifs.peek().else_flag);

    Server.processAction("int x = x + 1");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("} sonst {");
    assertFalse(Server.active_ifs.peek().if_flag);
    assertTrue(Server.active_ifs.peek().else_flag);

    Server.processAction("int x = x + 1");
    assertEquals(1, Server.variables.get("x").intVal);

    Server.processAction("}");
    assertTrue(Server.active_ifs.isEmpty());
    assertTrue(Server.active_scopes.isEmpty());
  }

  /**
   * Test if while-loops will be recognized correctly and execution of the while-body works
   * properly.
   */
  @Test
  public void testWhileEval() {
    Server.processAction("int x = 0;");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("solange (x < 2) {");
    assertTrue(Server.active_whiles.peek().conditionResult);
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("int x = x + 1");
    assertEquals(1, Server.active_whiles.peek().whileBody.size());

    Server.processAction("}");
    assertEquals(2, Server.variables.get("x").intVal);
    assertTrue(Server.active_whiles.isEmpty());
    assertTrue(Server.active_scopes.isEmpty());
  }

  /** Test while-loop with a condition that evaluates to false. */
  @Test
  public void testWhileFalseCondition() {
    Server.processAction("int x = 0;");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("solange (x > 2) {");
    assertFalse(Server.active_whiles.peek().conditionResult);
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("int x = x + 1");
    assertEquals(1, Server.active_whiles.peek().whileBody.size());

    Server.processAction("}");
    assertEquals(0, Server.variables.get("x").intVal);
    assertTrue(Server.active_whiles.isEmpty());
    assertTrue(Server.active_scopes.isEmpty());
  }

  /** Test nested while-loops. */
  @Test
  public void testNestedWhile() {
    Server.processAction("int x = 0;");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("solange (x < 2) {");
    assertTrue(Server.active_whiles.peek().conditionResult);
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("int x = x + 1");
    assertEquals(1, Server.active_whiles.peek().whileBody.size());

    Server.processAction("int y = 0");
    Server.processAction("solange (y < 3) {");
    assertEquals(2, Server.active_whiles.size());
    assertEquals("while", Server.active_scopes.peek());
    assertEquals(2, Server.active_scopes.size());

    Server.processAction("int y = y + 1");
    assertEquals(1, Server.active_whiles.peek().whileBody.size());

    Server.processAction("}");
    assertEquals(3, Server.variables.get("y").intVal);
    assertEquals(5, Server.active_whiles.peek().whileBody.size());
    assertEquals(1, Server.active_whiles.size());
    assertEquals(1, Server.active_scopes.size());
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("}");
    assertEquals(2, Server.variables.get("x").intVal);
    assertTrue(Server.active_whiles.isEmpty());
    assertTrue(Server.active_scopes.isEmpty());
  }

  /** Test nested whiles with a while-loop that evaluates to false at the top-level. */
  @Test
  public void testNestedWhileNegativeOuterCondition() {
    Server.processAction("int x = 0;");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("solange (x > 2) {");
    assertFalse(Server.active_whiles.peek().conditionResult);
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("int x = x + 1");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("int y = 0");
    Server.processAction("solange (y < 3) {");
    assertEquals(2, Server.active_whiles.size());
    assertEquals("while", Server.active_scopes.peek());
    assertEquals(2, Server.active_scopes.size());

    Server.processAction("int y = y + 1");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("}");
    assertNull(Server.variables.get("y"));
    assertEquals(5, Server.active_whiles.peek().whileBody.size());
    assertEquals(1, Server.active_whiles.size());
    assertEquals(1, Server.active_scopes.size());
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("}");
    assertEquals(0, Server.variables.get("x").intVal);
    assertTrue(Server.active_whiles.isEmpty());
    assertTrue(Server.active_scopes.isEmpty());
  }

  /**
   * Test if repeat-loops will be recognized correctly and execution of the repeat-body works
   * properly.
   */
  @Test
  public void testRepeatEval() {
    Server.processAction("int x = 0");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("wiederhole 4 Mal");
    Server.processAction("int x = x + 1");
    assertEquals("repeat", Server.active_scopes.peek());
    assertEquals(1, Server.active_repeats.peek().repeatBody.size());

    Server.processAction("}");
    assertEquals(4, Server.variables.get("x").intVal);
    assertTrue(Server.active_repeats.isEmpty());
    assertTrue(Server.active_scopes.isEmpty());
  }

  /** Test nested repeat loops. */
  @Test
  public void testNestedRepeat() {
    Server.processAction("int x = 0");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("wiederhole 4 Mal");
    Server.processAction("int x = x + 1");
    assertEquals("repeat", Server.active_scopes.peek());
    assertEquals(1, Server.active_repeats.peek().repeatBody.size());

    Server.processAction("int y = 0");
    Server.processAction("wiederhole 2 Mal");
    Server.processAction("int y = y + 1");
    assertEquals("repeat", Server.active_scopes.peek());
    assertEquals(2, Server.active_scopes.size());
    assertEquals(2, Server.active_repeats.size());

    Server.processAction("}");
    assertEquals("repeat", Server.active_scopes.peek());
    assertEquals(1, Server.active_scopes.size());
    assertEquals(1, Server.active_repeats.size());
    assertEquals(5, Server.active_repeats.peek().repeatBody.size());
    assertEquals(2, Server.variables.get("y").intVal);

    Server.processAction("}");
    assertEquals(4, Server.variables.get("x").intVal);
    assertTrue(Server.active_repeats.isEmpty());
    assertTrue(Server.active_scopes.isEmpty());
  }

  /** Test nested scopes of different types. */
  @Test
  public void testNestedScopes() {
    Server.processAction("wiederhole 4 Mal");
    assertEquals("repeat", Server.active_scopes.peek());

    Server.processAction("falls (wahr)");
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("solange (falsch)");
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("}");
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("}");
    assertEquals("repeat", Server.active_scopes.peek());

    Server.processAction("}");
    assertTrue(Server.active_scopes.isEmpty());
  }

  /** Test if func defs will be recognized correctly and the code will not actually be executed. */
  @Test
  public void testFuncEval() {
    Server.processAction("int x = 0");

    Server.processAction("public void dummy_func() {");
    Server.processAction("int x = x + 1");
    assertEquals("function", Server.active_scopes.peek());

    Server.processAction("falls (wahr)");
    Server.processAction("int x = x + 1");
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("solange (wahr)");
    Server.processAction("int x = x + 1");
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("wiederhole 3 Mal");
    Server.processAction("int x = x + 1");
    assertEquals("repeat", Server.active_scopes.peek());

    Server.processAction("}");
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("}");
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("}");
    assertEquals("function", Server.active_scopes.peek());

    Server.processAction("}");
    assertTrue(Server.active_scopes.isEmpty());
    assertEquals(0, Server.variables.get("x").intVal);
  }

  /**
   * Test if func calls will be recognized correctly and the func body will be executed properly.
   */
  @Test
  public void testFuncCall() {
    Server.processAction("int x = 0");

    Server.processAction("public void dummy_func() {");
    Server.processAction("int x = x + 1");
    assertEquals("function", Server.active_scopes.peek());

    Server.processAction("falls (wahr)");
    Server.processAction("int x = x + 1");
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("solange (x <= 2)");
    Server.processAction("int x = x + 1");
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("wiederhole 3 Mal");
    Server.processAction("int x = x + 1");
    assertEquals("repeat", Server.active_scopes.peek());

    Server.processAction("}");
    assertEquals("while", Server.active_scopes.peek());

    Server.processAction("}");
    assertEquals("if", Server.active_scopes.peek());

    Server.processAction("}");
    assertEquals("function", Server.active_scopes.peek());

    Server.processAction("}");
    assertTrue(Server.active_scopes.isEmpty());
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("dummy_func();");
    assertEquals(6, Server.variables.get("x").intVal);
  }

  /** Test func calls in func def. The code of the func call must not be executed. */
  @Test
  public void testFuncCallInFuncDef() {
    Server.processAction("int x = 0");
    Server.processAction("public void dummy_func() {");
    Server.processAction("int x = x + 1");
    Server.processAction("falls (wahr)");
    Server.processAction("int x = x + 1");
    Server.processAction("solange (x <= 2)");
    Server.processAction("int x = x + 1");
    Server.processAction("wiederhole 3 Mal");
    Server.processAction("int x = x + 1");
    Server.processAction("}");
    Server.processAction("}");
    Server.processAction("}");
    Server.processAction("}");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("public void dummy_func2() {");
    Server.processAction("int x = 5");
    Server.processAction("dummy_func()");
    assertEquals("function", Server.active_scopes.peek());
    Server.processAction("}");
    assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("dummy_func2();");
    assertEquals(7, Server.variables.get("x").intVal);
    Server.processAction("dummy_func();");
    assertEquals(9, Server.variables.get("x").intVal);
  }

  /**
   * Test if array creation will be recognized correctly and array variable will be created
   * properly.
   */
  @Test
  public void testArrayCreation() {
    Server.processAction("int[] array_a = new int[5];");
    int[] array_a = new int[5];
    assertArrayEquals(array_a, Server.variables.get("array_a").arrayVal);
  }

  /** Test if a value can be retrieved from an array properly. */
  @Test
  public void testArrayGet() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("int x = array_a[1];");
    assertEquals(0, Server.variables.get("x").intVal);
  }

  /** Test if a value can be set in an array properly. */
  @Test
  public void testArraySet() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("int array_a[1] = 5;");
    int[] array_a = new int[5];
    array_a[1] = 5;
    assertArrayEquals(array_a, Server.variables.get("array_a").arrayVal);
  }

  /** Test if the length of an array can be retrieved properly. */
  @Test
  public void testArrayLength() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("int x = array_a.length;");
    assertEquals(5, Server.variables.get("x").intVal);
  }

  /** Test if the variable not found error works properly. */
  @Test
  public void testVariableNotFoundError() {
    Server.processAction("int z = 5 + x");
    assertTrue(Server.errorOccurred);
    assertEquals("x is not a number or variable", Server.errorMsg);
    assertNull(Server.variables.get("z"));
    Server.clearGlobalValues();

    Server.processAction("int z = x[10]");
    assertTrue(Server.errorOccurred);
    assertEquals("Variable not found x", Server.errorMsg);
    assertNull(Server.variables.get("z"));
  }

  /** Test if the array is an array but a base variable was expected error works properly. */
  @Test
  public void testVariableIsArrayError() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("falls (array_a < 5)");
    assertTrue(Server.errorOccurred);
    assertEquals("Variable array_a is not a base type variable", Server.errorMsg);
    Server.clearGlobalValues();

    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("int z = array_a + 5;");
    assertTrue(Server.errorOccurred);
    assertEquals("Expected base variable. Got array for variable array_a", Server.errorMsg);
  }

  /** Test if an index out of bounds exception will be caught properly. */
  @Test
  public void testIndexOutOfBoundsError() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("int z = array_a[10]");
    assertTrue(Server.errorOccurred);
    assertEquals("Index 10 out of bounds for length 5", Server.errorMsg);
  }

  /**
   * Test if an error will be raised properly if an array variable was expected, but we got a base
   * variable.
   */
  @Test
  public void testVariableIsBaseError() {
    Server.processAction("int x = 5;");
    Server.processAction("int z = x[10]");
    assertTrue(Server.errorOccurred);
    assertEquals("Expected array variable. Got base for variable x", Server.errorMsg);
  }

  /** Test if we properly raise an error if we do not find a function. */
  @Test
  public void testFunctionNotFoundError() {
    Server.processAction("dummyFunc();");
    assertTrue(Server.errorOccurred);
    assertEquals("Function dummyFunc is not defined", Server.errorMsg);
  }

  /** Test if we properly raise an error when we can not find a variable in a condition. */
  @Test
  public void testConditionError() {
    Server.processAction("falls (array_a < 5)");
    assertTrue(Server.errorOccurred);
    assertEquals("Variable array_a could not be found", Server.errorMsg);
  }

  /**
   * Test if we properly raise an error when we got a variable with the wrong type in a condition.
   */
  @Test
  public void testConditionWrongVarType() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("falls (array_a < 5)");
    assertTrue(Server.errorOccurred);
    assertEquals("Variable array_a is not a base type variable", Server.errorMsg);
  }

  /** Test if we properly catch a division by zero and raise an error. */
  @Test
  public void testDivisionByZeroError() {
    Server.processAction("int a = 3 / 0");
    assertTrue(Server.errorOccurred);
    assertEquals("Division by zero is not allowed.", Server.errorMsg);
  }
}

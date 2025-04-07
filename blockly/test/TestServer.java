import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.regex.Pattern;
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
  public void testSimpleAssign() {
    server.processAction("int x = 5;");
    assertEquals(5, server.variables.get("x").intVal);
  }

  /** Test variable assign with expressions. */
  @Test
  public void testExpressionAssign() {
    server.processAction("int x = 5 + 5;");
    assertEquals(10, server.variables.get("x").intVal);
    server.processAction("int x = 5 * 5;");
    assertEquals(25, server.variables.get("x").intVal);
    server.processAction("int x = 5 - 5;");
    assertEquals(0, server.variables.get("x").intVal);
    server.processAction("int x = 5 / 5;");
    assertEquals(1, server.variables.get("x").intVal);
    server.processAction("int x = x + x;");
    assertEquals(2, server.variables.get("x").intVal);
    server.processAction("int x = x + 3;");
    assertEquals(5, server.variables.get("x").intVal);
    server.processAction("int x = 5 - x;");
    assertEquals(0, server.variables.get("x").intVal);
  }

  /** Test evaluation of conditions. */
  @Test
  public void testConditionEval() {
    Pattern pattern = Pattern.compile("(.*)");
    // Check for simple booleans
    assertFalse(server.evalComplexCondition("falsch", pattern));
    assertTrue(server.evalComplexCondition("wahr", pattern));
    assertTrue(server.evalComplexCondition("nicht falsch", pattern));
    assertFalse(server.evalComplexCondition("nicht wahr", pattern));
    // Compare operator
    assertTrue(server.evalComplexCondition("5 < 10", pattern));
    assertFalse(server.evalComplexCondition("5 > 10", pattern));
    assertFalse(server.evalComplexCondition("5 == 10", pattern));
    assertTrue(server.evalComplexCondition("5 != 10", pattern));
    // Edge cases
    assertFalse(server.evalComplexCondition("11 <= 10", pattern));
    assertTrue(server.evalComplexCondition("10 <= 10", pattern));
    assertTrue(server.evalComplexCondition("9 <= 10", pattern));
    assertTrue(server.evalComplexCondition("10 >= 10", pattern));
    assertTrue(server.evalComplexCondition("11 >= 10", pattern));
    assertFalse(server.evalComplexCondition("9 >= 10", pattern));
    // Logic operator
    assertTrue(server.evalComplexCondition("9 <= 10 && wahr", pattern));
    assertFalse(server.evalComplexCondition("nicht (9 <= 10 && wahr)", pattern));
    assertTrue(server.evalComplexCondition("9 >= 10 || wahr", pattern));
    // Complex condition
    assertTrue(
        server.evalComplexCondition(
            "(((nicht falsch && 10 >= 1) || (11 <= 10 && falsch)))", pattern));
  }

  /** Test if if-statements will be recognized correctly and if/else flag will be set properly. */
  @Test
  public void testIfEval() {
    server.processAction("falls (wahr)");
    assertTrue(server.active_ifs.peek().if_flag);
    assertFalse(server.active_ifs.peek().else_flag);
    assertEquals("if", server.active_scopes.peek());

    server.processAction("} sonst {");
    assertFalse(server.active_ifs.peek().if_flag);
    assertFalse(server.active_ifs.peek().else_flag);
    assertEquals("if", server.active_scopes.peek());

    server.processAction("}");
    assertTrue(server.active_ifs.isEmpty());
    assertTrue(server.active_scopes.isEmpty());
  }

  /** Test nested if-statements. */
  @Test
  public void testNestedIf() {
    server.processAction("falls (wahr)");
    assertTrue(server.active_ifs.peek().if_flag);
    assertFalse(server.active_ifs.peek().else_flag);
    assertEquals("if", server.active_scopes.peek());

    server.processAction("falls (falsch)");
    assertFalse(server.active_ifs.peek().if_flag);
    assertFalse(server.active_ifs.peek().else_flag);
    assertEquals(2, server.active_ifs.size());
    assertEquals("if", server.active_scopes.peek());
    assertEquals(2, server.active_scopes.size());

    server.processAction("}");
    assertTrue(server.active_ifs.peek().if_flag);
    assertFalse(server.active_ifs.peek().else_flag);
    assertEquals(1, server.active_ifs.size());
    assertEquals("if", server.active_scopes.peek());
    assertEquals(1, server.active_scopes.size());

    server.processAction("} sonst {");
    assertFalse(server.active_ifs.peek().if_flag);
    assertFalse(server.active_ifs.peek().else_flag);
    assertEquals("if", server.active_scopes.peek());

    server.processAction("}");
    assertTrue(server.active_ifs.isEmpty());
    assertTrue(server.active_scopes.isEmpty());
  }

  /** Test nested if-statements with a negative condition at the top-level. */
  @Test
  public void testNestedIfNegativeOuterCondition() {
    server.processAction("int x = 0;");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("falls (falsch)");
    assertFalse(server.active_ifs.peek().if_flag);
    assertFalse(server.active_ifs.peek().else_flag);
    assertEquals("if", server.active_scopes.peek());

    server.processAction("falls (wahr)");
    assertTrue(server.active_ifs.peek().if_flag);
    assertFalse(server.active_ifs.peek().else_flag);

    server.processAction("int x = x + 1");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("}");
    assertFalse(server.active_ifs.peek().if_flag);
    assertFalse(server.active_ifs.peek().else_flag);

    server.processAction("int x = x + 1");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("} sonst {");
    assertFalse(server.active_ifs.peek().if_flag);
    assertTrue(server.active_ifs.peek().else_flag);

    server.processAction("int x = x + 1");
    assertEquals(1, server.variables.get("x").intVal);

    server.processAction("}");
    assertTrue(server.active_ifs.isEmpty());
    assertTrue(server.active_scopes.isEmpty());
  }

  /**
   * Test if while-loops will be recognized correctly and execution of the while-body works
   * properly.
   */
  @Test
  public void testWhileEval() {
    server.processAction("int x = 0;");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("solange (x < 2) {");
    assertTrue(server.active_whiles.peek().conditionResult);
    assertEquals("while", server.active_scopes.peek());

    server.processAction("int x = x + 1");
    assertEquals(1, server.active_whiles.peek().whileBody.size());

    server.processAction("}");
    assertEquals(2, server.variables.get("x").intVal);
    assertTrue(server.active_whiles.isEmpty());
    assertTrue(server.active_scopes.isEmpty());
  }

  /** Test while-loop with a condition that evaluates to false. */
  @Test
  public void testWhileFalseCondition() {
    server.processAction("int x = 0;");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("solange (x > 2) {");
    assertFalse(server.active_whiles.peek().conditionResult);
    assertEquals("while", server.active_scopes.peek());

    server.processAction("int x = x + 1");
    assertEquals(1, server.active_whiles.peek().whileBody.size());

    server.processAction("}");
    assertEquals(0, server.variables.get("x").intVal);
    assertTrue(server.active_whiles.isEmpty());
    assertTrue(server.active_scopes.isEmpty());
  }

  /** Test nested while-loops. */
  @Test
  public void testNestedWhile() {
    server.processAction("int x = 0;");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("solange (x < 2) {");
    assertTrue(server.active_whiles.peek().conditionResult);
    assertEquals("while", server.active_scopes.peek());

    server.processAction("int x = x + 1");
    assertEquals(1, server.active_whiles.peek().whileBody.size());

    server.processAction("int y = 0");
    server.processAction("solange (y < 3) {");
    assertEquals(2, server.active_whiles.size());
    assertEquals("while", server.active_scopes.peek());
    assertEquals(2, server.active_scopes.size());

    server.processAction("int y = y + 1");
    assertEquals(1, server.active_whiles.peek().whileBody.size());

    server.processAction("}");
    assertEquals(3, server.variables.get("y").intVal);
    assertEquals(5, server.active_whiles.peek().whileBody.size());
    assertEquals(1, server.active_whiles.size());
    assertEquals(1, server.active_scopes.size());
    assertEquals("while", server.active_scopes.peek());

    server.processAction("}");
    assertEquals(2, server.variables.get("x").intVal);
    assertTrue(server.active_whiles.isEmpty());
    assertTrue(server.active_scopes.isEmpty());
  }

  /** Test nested whiles with a while-loop that evaluates to false at the top-level. */
  @Test
  public void testNestedWhileNegativeOuterCondition() {
    server.processAction("int x = 0;");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("solange (x > 2) {");
    assertFalse(server.active_whiles.peek().conditionResult);
    assertEquals("while", server.active_scopes.peek());

    server.processAction("int x = x + 1");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("int y = 0");
    server.processAction("solange (y < 3) {");
    assertEquals(2, server.active_whiles.size());
    assertEquals("while", server.active_scopes.peek());
    assertEquals(2, server.active_scopes.size());

    server.processAction("int y = y + 1");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("}");
    assertNull(server.variables.get("y"));
    assertEquals(5, server.active_whiles.peek().whileBody.size());
    assertEquals(1, server.active_whiles.size());
    assertEquals(1, server.active_scopes.size());
    assertEquals("while", server.active_scopes.peek());

    server.processAction("}");
    assertEquals(0, server.variables.get("x").intVal);
    assertTrue(server.active_whiles.isEmpty());
    assertTrue(server.active_scopes.isEmpty());
  }

  /**
   * Test if repeat-loops will be recognized correctly and execution of the repeat-body works
   * properly.
   */
  @Test
  public void testRepeatEval() {
    server.processAction("int x = 0");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("wiederhole 4 Mal");
    server.processAction("int x = x + 1");
    assertEquals("repeat", server.active_scopes.peek());
    assertEquals(1, server.active_repeats.peek().repeatBody.size());

    server.processAction("}");
    assertEquals(4, server.variables.get("x").intVal);
    assertTrue(server.active_repeats.isEmpty());
    assertTrue(server.active_scopes.isEmpty());
  }

  /** Test nested repeat loops. */
  @Test
  public void testNestedRepeat() {
    server.processAction("int x = 0");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("wiederhole 4 Mal");
    server.processAction("int x = x + 1");
    assertEquals("repeat", server.active_scopes.peek());
    assertEquals(1, server.active_repeats.peek().repeatBody.size());

    server.processAction("int y = 0");
    server.processAction("wiederhole 2 Mal");
    server.processAction("int y = y + 1");
    assertEquals("repeat", server.active_scopes.peek());
    assertEquals(2, server.active_scopes.size());
    assertEquals(2, server.active_repeats.size());

    server.processAction("}");
    assertEquals("repeat", server.active_scopes.peek());
    assertEquals(1, server.active_scopes.size());
    assertEquals(1, server.active_repeats.size());
    assertEquals(5, server.active_repeats.peek().repeatBody.size());
    assertEquals(2, server.variables.get("y").intVal);

    server.processAction("}");
    assertEquals(4, server.variables.get("x").intVal);
    assertTrue(server.active_repeats.isEmpty());
    assertTrue(server.active_scopes.isEmpty());
  }

  /** Test nested scopes of different types. */
  @Test
  public void testNestedScopes() {
    server.processAction("wiederhole 4 Mal");
    assertEquals("repeat", server.active_scopes.peek());

    server.processAction("falls (wahr)");
    assertEquals("if", server.active_scopes.peek());

    server.processAction("solange (falsch)");
    assertEquals("while", server.active_scopes.peek());

    server.processAction("}");
    assertEquals("if", server.active_scopes.peek());

    server.processAction("}");
    assertEquals("repeat", server.active_scopes.peek());

    server.processAction("}");
    assertTrue(server.active_scopes.isEmpty());
  }

  /** Test if func defs will be recognized correctly and the code will not actually be executed. */
  @Test
  public void testFuncEval() {
    server.processAction("int x = 0");

    server.processAction("public void dummy_func() {");
    server.processAction("int x = x + 1");
    assertEquals("function", server.active_scopes.peek());

    server.processAction("falls (wahr)");
    server.processAction("int x = x + 1");
    assertEquals("if", server.active_scopes.peek());

    server.processAction("solange (wahr)");
    server.processAction("int x = x + 1");
    assertEquals("while", server.active_scopes.peek());

    server.processAction("wiederhole 3 Mal");
    server.processAction("int x = x + 1");
    assertEquals("repeat", server.active_scopes.peek());

    server.processAction("}");
    assertEquals("while", server.active_scopes.peek());

    server.processAction("}");
    assertEquals("if", server.active_scopes.peek());

    server.processAction("}");
    assertEquals("function", server.active_scopes.peek());

    server.processAction("}");
    assertTrue(server.active_scopes.isEmpty());
    assertEquals(0, server.variables.get("x").intVal);
  }

  /**
   * Test if func calls will be recognized correctly and the func body will be executed properly.
   */
  @Test
  public void testFuncCall() {
    server.processAction("int x = 0");

    server.processAction("public void dummy_func() {");
    server.processAction("int x = x + 1");
    assertEquals("function", server.active_scopes.peek());

    server.processAction("falls (wahr)");
    server.processAction("int x = x + 1");
    assertEquals("if", server.active_scopes.peek());

    server.processAction("solange (x <= 2)");
    server.processAction("int x = x + 1");
    assertEquals("while", server.active_scopes.peek());

    server.processAction("wiederhole 3 Mal");
    server.processAction("int x = x + 1");
    assertEquals("repeat", server.active_scopes.peek());

    server.processAction("}");
    assertEquals("while", server.active_scopes.peek());

    server.processAction("}");
    assertEquals("if", server.active_scopes.peek());

    server.processAction("}");
    assertEquals("function", server.active_scopes.peek());

    server.processAction("}");
    assertTrue(server.active_scopes.isEmpty());
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("dummy_func();");
    assertEquals(6, server.variables.get("x").intVal);
  }

  /** Test func calls in func def. The code of the func call must not be executed. */
  @Test
  public void testFuncCallInFuncDef() {
    server.processAction("int x = 0");
    server.processAction("public void dummy_func() {");
    server.processAction("int x = x + 1");
    server.processAction("falls (wahr)");
    server.processAction("int x = x + 1");
    server.processAction("solange (x <= 2)");
    server.processAction("int x = x + 1");
    server.processAction("wiederhole 3 Mal");
    server.processAction("int x = x + 1");
    server.processAction("}");
    server.processAction("}");
    server.processAction("}");
    server.processAction("}");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("public void dummy_func2() {");
    server.processAction("int x = 5");
    server.processAction("dummy_func()");
    assertEquals("function", server.active_scopes.peek());
    server.processAction("}");
    assertEquals(0, server.variables.get("x").intVal);

    server.processAction("dummy_func2();");
    assertEquals(7, server.variables.get("x").intVal);
    server.processAction("dummy_func();");
    assertEquals(9, server.variables.get("x").intVal);
  }

  /**
   * Test if array creation will be recognized correctly and array variable will be created
   * properly.
   */
  @Test
  public void testArrayCreation() {
    server.processAction("int[] array_a = new int[5];");
    int[] array_a = new int[5];
    assertArrayEquals(array_a, server.variables.get("array_a").arrayVal);
  }

  /** Test if a value can be retrieved from an array properly. */
  @Test
  public void testArrayGet() {
    server.processAction("int[] array_a = new int[5];");
    server.processAction("int x = array_a[1];");
    assertEquals(0, server.variables.get("x").intVal);
  }

  /** Test if a value can be set in an array properly. */
  @Test
  public void testArraySet() {
    server.processAction("int[] array_a = new int[5];");
    server.processAction("int array_a[1] = 5;");
    int[] array_a = new int[5];
    array_a[1] = 5;
    assertArrayEquals(array_a, server.variables.get("array_a").arrayVal);
  }

  /** Test if the length of an array can be retrieved properly. */
  @Test
  public void testArrayLength() {
    server.processAction("int[] array_a = new int[5];");
    server.processAction("int x = array_a.length;");
    assertEquals(5, server.variables.get("x").intVal);
  }

  /** Test if the variable not found error works properly. */
  @Test
  public void testVariableNotFoundError() {
    server.processAction("int z = 5 + x");
    assertTrue(server.errorOccurred);
    assertEquals("x is not a number or variable", server.errorMsg);
    assertNull(server.variables.get("z"));
    server.clearGlobalValues();

    server.processAction("int z = x[10]");
    assertTrue(server.errorOccurred);
    assertEquals("Variable not found x", server.errorMsg);
    assertNull(server.variables.get("z"));
  }

  /** Test if the array is an array but a base variable was expected error works properly. */
  @Test
  public void testVariableIsArrayError() {
    server.processAction("int[] array_a = new int[5];");
    server.processAction("falls (array_a < 5)");
    assertTrue(server.errorOccurred);
    assertEquals("Variable array_a is not a base type variable", server.errorMsg);
    server.clearGlobalValues();

    server.processAction("int[] array_a = new int[5];");
    server.processAction("int z = array_a + 5;");
    assertTrue(server.errorOccurred);
    assertEquals("Expected base variable. Got array for variable array_a", server.errorMsg);
  }

  /** Test if an index out of bounds exception will be caught properly. */
  @Test
  public void testIndexOutOfBoundsError() {
    server.processAction("int[] array_a = new int[5];");
    server.processAction("int z = array_a[10]");
    assertTrue(server.errorOccurred);
    assertEquals("Index 10 out of bounds for length 5", server.errorMsg);
  }

  /**
   * Test if an error will be raised properly if an array variable was expected, but we got a base
   * variable.
   */
  @Test
  public void testVariableIsBaseError() {
    server.processAction("int x = 5;");
    server.processAction("int z = x[10]");
    assertTrue(server.errorOccurred);
    assertEquals("Expected array variable. Got base for variable x", server.errorMsg);
  }

  /** Test if we properly raise an error if we do not find a function. */
  @Test
  public void testFunctionNotFoundError() {
    server.processAction("dummyFunc();");
    assertTrue(server.errorOccurred);
    assertEquals("Function dummyFunc is not defined", server.errorMsg);
  }

  /** Test if we properly raise an error when we can not find a variable in a condition. */
  @Test
  public void testConditionError() {
    server.processAction("falls (array_a < 5)");
    assertTrue(server.errorOccurred);
    assertEquals("Variable array_a could not be found", server.errorMsg);
  }

  /**
   * Test if we properly raise an error when we got a variable with the wrong type in a condition.
   */
  @Test
  public void testConditionWrongVarType() {
    server.processAction("int[] array_a = new int[5];");
    server.processAction("falls (array_a < 5)");
    assertTrue(server.errorOccurred);
    assertEquals("Variable array_a is not a base type variable", server.errorMsg);
  }

  /** Test if we properly catch a division by zero and raise an error. */
  @Test
  public void testDivisionByZeroError() {
    server.processAction("int a = 3 / 0");
    assertTrue(server.errorOccurred);
    assertEquals("Division by zero is not allowed.", server.errorMsg);
  }
}

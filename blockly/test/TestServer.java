import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import server.Server;

public class TestServer {

  @Before
  public void setUp() {
    Server.clearGlobalValues();
  }

  @Test
  public void testSimpleAssign() {
    Server.processAction("int x = 5;");
    Assert.assertEquals(5, Server.variables.get("x").intVal);
  }

  @Test
  public void testExpressionAssign() {
    Server.processAction("int x = 5 + 5;");
    Assert.assertEquals(10, Server.variables.get("x").intVal);
    Server.processAction("int x = 5 * 5;");
    Assert.assertEquals(25, Server.variables.get("x").intVal);
    Server.processAction("int x = 5 - 5;");
    Assert.assertEquals(0, Server.variables.get("x").intVal);
    Server.processAction("int x = 5 / 5;");
    Assert.assertEquals(1, Server.variables.get("x").intVal);
    Server.processAction("int x = x + x;");
    Assert.assertEquals(2, Server.variables.get("x").intVal);
    Server.processAction("int x = x + 3;");
    Assert.assertEquals(5, Server.variables.get("x").intVal);
    Server.processAction("int x = 5 - x;");
    Assert.assertEquals(0, Server.variables.get("x").intVal);
  }

  @Test
  public void testConditionEval() {
    Pattern pattern = Pattern.compile("(.*)");
    // Check for simple booleans
    Assert.assertFalse(Server.evalComplexCondition("falsch", pattern));
    Assert.assertTrue(Server.evalComplexCondition("wahr", pattern));
    Assert.assertTrue(Server.evalComplexCondition("nicht falsch", pattern));
    Assert.assertFalse(Server.evalComplexCondition("nicht wahr", pattern));
    // Compare operator
    Assert.assertTrue(Server.evalComplexCondition("5 < 10", pattern));
    Assert.assertFalse(Server.evalComplexCondition("5 > 10", pattern));
    Assert.assertFalse(Server.evalComplexCondition("5 == 10", pattern));
    Assert.assertTrue(Server.evalComplexCondition("5 != 10", pattern));
    // Edge cases
    Assert.assertFalse(Server.evalComplexCondition("11 <= 10", pattern));
    Assert.assertTrue(Server.evalComplexCondition("10 <= 10", pattern));
    Assert.assertTrue(Server.evalComplexCondition("9 <= 10", pattern));
    Assert.assertTrue(Server.evalComplexCondition("10 >= 10", pattern));
    Assert.assertTrue(Server.evalComplexCondition("11 >= 10", pattern));
    Assert.assertFalse(Server.evalComplexCondition("9 >= 10", pattern));
    // Logic operator
    Assert.assertTrue(Server.evalComplexCondition("9 <= 10 && wahr", pattern));
    Assert.assertFalse(Server.evalComplexCondition("nicht (9 <= 10 && wahr)", pattern));
    Assert.assertTrue(Server.evalComplexCondition("9 >= 10 || wahr", pattern));
    // Complex condition
    Assert.assertTrue(
        Server.evalComplexCondition(
            "(((nicht falsch && 10 >= 1) || (11 <= 10 && falsch)))", pattern));
  }

  @Test
  public void testIfEval() {
    Server.processAction("falls (wahr)");
    Assert.assertTrue(Server.active_ifs.peek().if_flag);
    Assert.assertFalse(Server.active_ifs.peek().else_flag);
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("} sonst {");
    Assert.assertFalse(Server.active_ifs.peek().if_flag);
    Assert.assertFalse(Server.active_ifs.peek().else_flag);
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertTrue(Server.active_ifs.isEmpty());
    Assert.assertTrue(Server.active_scopes.isEmpty());
  }

  @Test
  public void testNestedIf() {
    Server.processAction("falls (wahr)");
    Assert.assertTrue(Server.active_ifs.peek().if_flag);
    Assert.assertFalse(Server.active_ifs.peek().else_flag);
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("falls (falsch)");
    Assert.assertFalse(Server.active_ifs.peek().if_flag);
    Assert.assertFalse(Server.active_ifs.peek().else_flag);
    Assert.assertEquals(2, Server.active_ifs.size());
    Assert.assertEquals("if", Server.active_scopes.peek());
    Assert.assertEquals(2, Server.active_scopes.size());

    Server.processAction("}");
    Assert.assertTrue(Server.active_ifs.peek().if_flag);
    Assert.assertFalse(Server.active_ifs.peek().else_flag);
    Assert.assertEquals(1, Server.active_ifs.size());
    Assert.assertEquals("if", Server.active_scopes.peek());
    Assert.assertEquals(1, Server.active_scopes.size());

    Server.processAction("} sonst {");
    Assert.assertFalse(Server.active_ifs.peek().if_flag);
    Assert.assertFalse(Server.active_ifs.peek().else_flag);
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertTrue(Server.active_ifs.isEmpty());
    Assert.assertTrue(Server.active_scopes.isEmpty());
  }

  @Test
  public void testNestedIfNegativeOuterCondition() {
    Server.processAction("int x = 0;");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("falls (falsch)");
    Assert.assertFalse(Server.active_ifs.peek().if_flag);
    Assert.assertFalse(Server.active_ifs.peek().else_flag);
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("falls (wahr)");
    Assert.assertTrue(Server.active_ifs.peek().if_flag);
    Assert.assertFalse(Server.active_ifs.peek().else_flag);

    Server.processAction("int x = x + 1");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("}");
    Assert.assertFalse(Server.active_ifs.peek().if_flag);
    Assert.assertFalse(Server.active_ifs.peek().else_flag);

    Server.processAction("int x = x + 1");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("} sonst {");
    Assert.assertFalse(Server.active_ifs.peek().if_flag);
    Assert.assertTrue(Server.active_ifs.peek().else_flag);

    Server.processAction("int x = x + 1");
    Assert.assertEquals(1, Server.variables.get("x").intVal);

    Server.processAction("}");
    Assert.assertTrue(Server.active_ifs.isEmpty());
    Assert.assertTrue(Server.active_scopes.isEmpty());
  }

  @Test
  public void testWhileEval() {
    Server.processAction("int x = 0;");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("solange (x < 2) {");
    Assert.assertTrue(Server.active_whiles.peek().conditionResult);
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("int x = x + 1");
    Assert.assertEquals(1, Server.active_whiles.peek().whileBody.size());

    Server.processAction("}");
    Assert.assertEquals(2, Server.variables.get("x").intVal);
    Assert.assertTrue(Server.active_whiles.isEmpty());
    Assert.assertTrue(Server.active_scopes.isEmpty());
  }

  @Test
  public void testWhileFalseCondition() {
    Server.processAction("int x = 0;");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("solange (x > 2) {");
    Assert.assertFalse(Server.active_whiles.peek().conditionResult);
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("int x = x + 1");
    Assert.assertEquals(1, Server.active_whiles.peek().whileBody.size());

    Server.processAction("}");
    Assert.assertEquals(0, Server.variables.get("x").intVal);
    Assert.assertTrue(Server.active_whiles.isEmpty());
    Assert.assertTrue(Server.active_scopes.isEmpty());
  }

  @Test
  public void testNestedWhile() {
    Server.processAction("int x = 0;");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("solange (x < 2) {");
    Assert.assertTrue(Server.active_whiles.peek().conditionResult);
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("int x = x + 1");
    Assert.assertEquals(1, Server.active_whiles.peek().whileBody.size());

    Server.processAction("int y = 0");
    Server.processAction("solange (y < 3) {");
    Assert.assertEquals(2, Server.active_whiles.size());
    Assert.assertEquals("while", Server.active_scopes.peek());
    Assert.assertEquals(2, Server.active_scopes.size());

    Server.processAction("int y = y + 1");
    Assert.assertEquals(1, Server.active_whiles.peek().whileBody.size());

    Server.processAction("}");
    Assert.assertEquals(3, Server.variables.get("y").intVal);
    Assert.assertEquals(5, Server.active_whiles.peek().whileBody.size());
    Assert.assertEquals(1, Server.active_whiles.size());
    Assert.assertEquals(1, Server.active_scopes.size());
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertEquals(2, Server.variables.get("x").intVal);
    Assert.assertTrue(Server.active_whiles.isEmpty());
    Assert.assertTrue(Server.active_scopes.isEmpty());
  }

  @Test
  public void testNestedWhileNegativeOuterCondition() {
    Server.processAction("int x = 0;");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("solange (x > 2) {");
    Assert.assertFalse(Server.active_whiles.peek().conditionResult);
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("int x = x + 1");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("int y = 0");
    Server.processAction("solange (y < 3) {");
    Assert.assertEquals(2, Server.active_whiles.size());
    Assert.assertEquals("while", Server.active_scopes.peek());
    Assert.assertEquals(2, Server.active_scopes.size());

    Server.processAction("int y = y + 1");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("}");
    Assert.assertNull(Server.variables.get("y"));
    Assert.assertEquals(5, Server.active_whiles.peek().whileBody.size());
    Assert.assertEquals(1, Server.active_whiles.size());
    Assert.assertEquals(1, Server.active_scopes.size());
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertEquals(0, Server.variables.get("x").intVal);
    Assert.assertTrue(Server.active_whiles.isEmpty());
    Assert.assertTrue(Server.active_scopes.isEmpty());
  }

  @Test
  public void testRepeatEval() {
    Server.processAction("int x = 0");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("wiederhole 4 Mal");
    Server.processAction("int x = x + 1");
    Assert.assertEquals("repeat", Server.active_scopes.peek());
    Assert.assertEquals(1, Server.active_repeats.peek().repeatBody.size());

    Server.processAction("}");
    Assert.assertEquals(4, Server.variables.get("x").intVal);
    Assert.assertTrue(Server.active_repeats.isEmpty());
    Assert.assertTrue(Server.active_scopes.isEmpty());
  }

  @Test
  public void testNestedRepeat() {
    Server.processAction("int x = 0");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("wiederhole 4 Mal");
    Server.processAction("int x = x + 1");
    Assert.assertEquals("repeat", Server.active_scopes.peek());
    Assert.assertEquals(1, Server.active_repeats.peek().repeatBody.size());

    Server.processAction("int y = 0");
    Server.processAction("wiederhole 2 Mal");
    Server.processAction("int y = y + 1");
    Assert.assertEquals("repeat", Server.active_scopes.peek());
    Assert.assertEquals(2, Server.active_scopes.size());
    Assert.assertEquals(2, Server.active_repeats.size());

    Server.processAction("}");
    Assert.assertEquals("repeat", Server.active_scopes.peek());
    Assert.assertEquals(1, Server.active_scopes.size());
    Assert.assertEquals(1, Server.active_repeats.size());
    Assert.assertEquals(5, Server.active_repeats.peek().repeatBody.size());
    Assert.assertEquals(2, Server.variables.get("y").intVal);

    Server.processAction("}");
    Assert.assertEquals(4, Server.variables.get("x").intVal);
    Assert.assertTrue(Server.active_repeats.isEmpty());
    Assert.assertTrue(Server.active_scopes.isEmpty());
  }

  @Test
  public void testNestedScopes() {
    Server.processAction("wiederhole 4 Mal");
    Assert.assertEquals("repeat", Server.active_scopes.peek());

    Server.processAction("falls (wahr)");
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("solange (falsch)");
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertEquals("repeat", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertTrue(Server.active_scopes.isEmpty());
  }

  @Test
  public void testFuncEval() {
    Server.processAction("int x = 0");

    Server.processAction("public void dummy_func() {");
    Server.processAction("int x = x + 1");
    Assert.assertEquals("function", Server.active_scopes.peek());

    Server.processAction("falls (wahr)");
    Server.processAction("int x = x + 1");
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("solange (wahr)");
    Server.processAction("int x = x + 1");
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("wiederhole 3 Mal");
    Server.processAction("int x = x + 1");
    Assert.assertEquals("repeat", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertEquals("function", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertTrue(Server.active_scopes.isEmpty());
    Assert.assertEquals(0, Server.variables.get("x").intVal);
  }

  @Test
  public void testFuncCall() {
    Server.processAction("int x = 0");

    Server.processAction("public void dummy_func() {");
    Server.processAction("int x = x + 1");
    Assert.assertEquals("function", Server.active_scopes.peek());

    Server.processAction("falls (wahr)");
    Server.processAction("int x = x + 1");
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("solange (x <= 2)");
    Server.processAction("int x = x + 1");
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("wiederhole 3 Mal");
    Server.processAction("int x = x + 1");
    Assert.assertEquals("repeat", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertEquals("while", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertEquals("if", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertEquals("function", Server.active_scopes.peek());

    Server.processAction("}");
    Assert.assertTrue(Server.active_scopes.isEmpty());
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("dummy_func();");
    Assert.assertEquals(6, Server.variables.get("x").intVal);
  }

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
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("public void dummy_func2() {");
    Server.processAction("int x = 5");
    Server.processAction("dummy_func()");
    Assert.assertEquals("function", Server.active_scopes.peek());
    Server.processAction("}");
    Assert.assertEquals(0, Server.variables.get("x").intVal);

    Server.processAction("dummy_func2();");
    Assert.assertEquals(7, Server.variables.get("x").intVal);
    Server.processAction("dummy_func();");
    Assert.assertEquals(9, Server.variables.get("x").intVal);
  }

  @Test
  public void testArrayCreation() {
    Server.processAction("int[] array_a = new int[5];");
    int[] array_a = new int[5];
    Assert.assertArrayEquals(array_a, Server.variables.get("array_a").arrayVal);
  }

  @Test
  public void testArrayGet() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("int x = array_a[1];");
    Assert.assertEquals(0, Server.variables.get("x").intVal);
  }

  @Test
  public void testArraySet() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("int array_a[1] = 5;");
    int[] array_a = new int[5];
    array_a[1] = 5;
    Assert.assertArrayEquals(array_a, Server.variables.get("array_a").arrayVal);
  }

  @Test
  public void testArrayLength() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("int x = array_a.length;");
    Assert.assertEquals(5, Server.variables.get("x").intVal);
  }

  @Test
  public void testVariableNotFoundError() {
    Server.processAction("int z = 5 + x");
    Assert.assertTrue(Server.errorOccurred);
    Assert.assertEquals("x is not a number or variable", Server.errorMsg);
    Assert.assertNull(Server.variables.get("z"));
    Server.clearGlobalValues();

    Server.processAction("int z = x[10]");
    Assert.assertTrue(Server.errorOccurred);
    Assert.assertEquals("Variable not found x", Server.errorMsg);
    Assert.assertNull(Server.variables.get("z"));
  }

  @Test
  public void testVariableIsArrayError() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("falls (array_a < 5)");
    Assert.assertTrue(Server.errorOccurred);
    Assert.assertEquals("Variable array_a is not a base type variable", Server.errorMsg);
    Server.clearGlobalValues();

    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("int z = array_a + 5;");
    Assert.assertTrue(Server.errorOccurred);
    Assert.assertEquals("Expected base variable. Got array for variable array_a", Server.errorMsg);
  }

  @Test
  public void testIndexOutOfBoundsError() {
    Server.processAction("int[] array_a = new int[5];");
    Server.processAction("int z = array_a[10]");
    Assert.assertTrue(Server.errorOccurred);
    Assert.assertEquals("Index 10 out of bounds for length 5", Server.errorMsg);
  }

  @Test
  public void testVariableIsBaseError() {
    Server.processAction("int x = 5;");
    Server.processAction("int z = x[10]");
    Assert.assertTrue(Server.errorOccurred);
    Assert.assertEquals("Expected array variable. Got base for variable x", Server.errorMsg);
  }

  @Test
  public void testFunctionNotFoundError() {
    Server.processAction("dummyFunc();");
    Assert.assertTrue(Server.errorOccurred);
    Assert.assertEquals("Function dummyFunc is not defined", Server.errorMsg);
  }

  @Test
  public void testConditionError() {
    Server.processAction("falls (array_a < 5)");
    Assert.assertTrue(Server.errorOccurred);
    Assert.assertEquals("Variable array_a could not be found", Server.errorMsg);
  }

  @Test
  public void testDivisionByZeroError() {
    Server.processAction("int a = 3 / 0");
    Assert.assertTrue(Server.errorOccurred);
    Assert.assertEquals("Division by zero is not allowed.", Server.errorMsg);
  }
}

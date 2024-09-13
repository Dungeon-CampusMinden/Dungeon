import org.junit.Before;
import org.junit.Test;

import org.junit.Assert;
import server.Server;

import java.util.regex.Pattern;

public class TestServer {

  @Before
  public void setUp() {
      Server.clearGlobalValues();
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
    Assert.assertTrue(Server.evalComplexCondition("(((nicht falsch && 10 >= 1) || (11 <= 10 && falsch)))", pattern));
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
    Assert.assertEquals(2,  Server.variables.get("y").intVal);

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

}

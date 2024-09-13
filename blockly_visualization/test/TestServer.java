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
  public void ifEval() {
    String action = "falls (wahr)";
    Server.processAction(action);
  }
}

import core.Dialect;
import core.ir.SourceLocation;
import dialect.arith.ConstantOp;
import dialect.scf.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Simple test to debug SCF operations */
public class SimpleScfTest {
  static final SourceLocation LOC = SourceLocation.UNKNOWN;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
  }

  @Test
  public void testScopeOpDirectly() {
    ScopeOp scopeOp = new ScopeOp(LOC);
    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    System.out.println("Scope verify: " + scopeOp.verify(true));
    System.out.println("Has terminator: " + scopeOp.getRegion().getEntryBlock().hasTerminator());
  }

  @Test
  public void testIfOpDirectly() {
    var condOp = new ConstantOp(LOC, true);
    IfOp ifOp = new IfOp(LOC, condOp.getValue(), false);

    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    System.out.println("ConstOp verify: " + condOp.verify(true));
    System.out.println("If verify: " + ifOp.verify(true));
    System.out.println(
        "Then has terminator: " + ifOp.getThenRegion().getEntryBlock().hasTerminator());
  }

  @Test
  public void testForOpDirectly() {
    var initValue = new ConstantOp(LOC, 0);
    var lowerBound = new ConstantOp(LOC, 0);
    var upperBound = new ConstantOp(LOC, 10);
    var step = new ConstantOp(LOC, 1);

    ForOp forOp =
        new ForOp(
            LOC, initValue.getValue(), lowerBound.getValue(), upperBound.getValue(), step.getValue());

    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    System.out.println("For verify: " + forOp.verify(true));
    System.out.println("Has induction value: " + true);
    System.out.println("For has terminator: " + forOp.getRegion().getEntryBlock().hasTerminator());
  }
}

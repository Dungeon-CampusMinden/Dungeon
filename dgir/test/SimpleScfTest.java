import dgir.core.Dialect;
import dgir.core.debug.Location;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.scf.ScfOps.*;

/** Simple test to debug SCF operations */
public class SimpleScfTest {
  static final Location LOC = Location.UNKNOWN;

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
    IfOp ifOp = new IfOp(LOC, condOp.getResult(), false);

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
            LOC,
            initValue.getResult(),
            lowerBound.getResult(),
            upperBound.getResult(),
            step.getResult());

    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    System.out.println("For verify: " + forOp.verify(true));
    System.out.println("Has induction value: " + true);
    System.out.println("For has terminator: " + forOp.getRegion().getEntryBlock().hasTerminator());
  }
}

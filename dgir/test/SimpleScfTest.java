import core.Dialect;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.io.PrintOp;
import dialect.scf.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Simple test to debug SCF operations
 */
public class SimpleScfTest {
  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
  }

  @Test
  public void testScopeOpDirectly() {
    ScopeOp scopeOp = new ScopeOp();
    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    System.out.println("Scope verify: " + scopeOp.verify(true));
    System.out.println("Has terminator: " + scopeOp.getRegion().getEntryBlock().hasTerminator());
  }

  @Test
  public void testIfOpDirectly() {
    var condOp = new ConstantOp(true);
    IfOp ifOp = new IfOp(condOp.getOutputValue(), false);

    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    System.out.println("ConstOp verify: " + condOp.verify(true));
    System.out.println("If verify: " + ifOp.verify(true));
    System.out.println("Then has terminator: " + ifOp.getThenRegion().getEntryBlock().hasTerminator());
  }

  @Test
  public void testForOpDirectly() {
    var initValue = new ConstantOp(0);
    var lowerBound = new ConstantOp(0);
    var upperBound = new ConstantOp(10);
    var step = new ConstantOp(1);

    ForOp forOp = new ForOp(
      initValue.getOutputValue(),
      lowerBound.getOutputValue(),
      upperBound.getOutputValue(),
      step.getOutputValue()
    );

    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    System.out.println("For verify: " + forOp.verify(true));
    System.out.println("Has induction value: " + (forOp.getInductionValue() != null));
    System.out.println("For has terminator: " + forOp.getRegion().getEntryBlock().hasTerminator());
  }
}


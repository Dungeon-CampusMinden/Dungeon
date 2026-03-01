import core.Dialect;
import core.debug.Location;
import core.serialization.Utils;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.func.types.FuncType;
import dialect.io.PrintOp;
import dialect.scf.ContinueOp;
import dialect.scf.ForOp;
import dialect.scf.IfOp;
import dialect.scf.ScopeOp;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static dialect.arith.ArithOps.*;
import static dialect.builtin.BuiltinOps.*;
import static dialect.builtin.BuiltinTypes.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The test cases for the SCF dialect. These are mostly focused on testing the control flow and
 * region handling of the dialect, as well as the basic functionality of the ScopeOp. It also test
 * that reaching definitions hold in complex cases and some negative test cases.
 */
public class ScfTests {
  static final Location LOC = Location.UNKNOWN;
  static ObjectMapper mapper;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
    mapper = Utils.getMapper(true);
  }

  // ===================== ScopeOp Tests =====================

  @Test
  public void simpleScopeOp() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(LOC), 0);
    var constOp = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 42));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, constOp.getValue()));
    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void nestedScopeOps() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp outerScope = funcOp.addOperation(new ScopeOp(LOC), 0);
    var outerConst = outerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 10));

    ScopeOp innerScope = outerScope.getRegion().getEntryBlock().addOperation(new ScopeOp(LOC));
    var innerConst = innerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 20));
    // Inner scope can use values from outer scope
    innerScope.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, outerConst.getValue()));
    innerScope.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, innerConst.getValue()));
    innerScope.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    outerScope.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void scopeOpWithMultipleOperations() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(LOC), 0);
    var const1 = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 1));
    var const2 = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 2));
    var const3 = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 3));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, const1.getValue()));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, const2.getValue()));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, const3.getValue()));
    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void scopeOpMissingTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(LOC), 0);
    var constOp = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 42));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, constOp.getValue()));
    // Missing ContinueOp

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  // ===================== IfOp Tests =====================

  @Test
  public void simpleIfOpWithThen() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(LOC, condOp.getValue(), false), 0);

    var thenConst = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 42));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, thenConst.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void ifOpWithThenAndElse() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(LOC, condOp.getValue(), true), 0);

    // Then branch
    var thenConst =
        ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Then branch"));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, thenConst.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // Else branch
    var elseConst =
        ifOp.getElseRegion().get().getEntryBlock().addOperation(new ConstantOp(LOC, "Else branch"));
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new PrintOp(LOC, elseConst.getValue()));
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void nestedIfOps() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var outerCond = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp outerIf = funcOp.addOperation(new IfOp(LOC, outerCond.getValue(), false), 0);

    // Nested if inside outer then
    var innerCond =
        outerIf.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, false));
    IfOp innerIf =
        outerIf
            .getThenRegion()
            .getEntryBlock()
            .addOperation(new IfOp(LOC, innerCond.getValue(), true));

    // Inner then
    var innerThenConst =
        innerIf.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Inner then"));
    innerIf
        .getThenRegion()
        .getEntryBlock()
        .addOperation(new PrintOp(LOC, innerThenConst.getValue()));
    innerIf.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // Inner else
    var innerElseConst =
        innerIf
            .getElseRegion()
            .get()
            .getEntryBlock()
            .addOperation(new ConstantOp(LOC, "Inner else"));
    innerIf
        .getElseRegion()
        .get()
        .getEntryBlock()
        .addOperation(new PrintOp(LOC, innerElseConst.getValue()));
    innerIf.getElseRegion().get().getEntryBlock().addOperation(new ContinueOp(LOC));

    outerIf.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void ifOpWithValueFromOuterScope() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var outerValue = funcOp.addOperation(new ConstantOp(LOC, 100), 0);
    var condOp = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(LOC, condOp.getValue(), true), 0);

    // Both branches should be able to use the outer value
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, outerValue.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    ifOp.getElseRegion()
        .get()
        .getEntryBlock()
        .addOperation(new PrintOp(LOC, outerValue.getValue()));
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void ifOpMissingThenTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(LOC, condOp.getValue(), false), 0);

    var thenConst = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 42));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, thenConst.getValue()));
    // Missing ContinueOp

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void ifOpMissingElseTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(LOC, condOp.getValue(), true), 0);

    // Then branch is OK
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // Else branch missing terminator
    var elseConst =
        ifOp.getElseRegion().get().getEntryBlock().addOperation(new ConstantOp(LOC, 42));
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new PrintOp(LOC, elseConst.getValue()));
    // Missing ContinueOp

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  // ===================== ForOp Tests =====================

  @Test
  public void simpleForOp() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(LOC, 10), 0);
    var step = funcOp.addOperation(new ConstantOp(LOC, 1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                LOC,
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    // Use the induction variable
    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, forOp.getInductionValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void nestedForOps() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    // Outer loop
    var outerInit = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var outerLower = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var outerUpper = funcOp.addOperation(new ConstantOp(LOC, 5), 0);
    var outerStep = funcOp.addOperation(new ConstantOp(LOC, 1), 0);

    ForOp outerFor =
        funcOp.addOperation(
            new ForOp(
                LOC,
                outerInit.getValue(),
                outerLower.getValue(),
                outerUpper.getValue(),
                outerStep.getValue()),
            0);

    // Inner loop
    var innerInit = outerFor.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 0));
    var innerLower = outerFor.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 0));
    var innerUpper = outerFor.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 3));
    var innerStep = outerFor.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 1));

    ForOp innerFor =
        outerFor
            .getRegion()
            .getEntryBlock()
            .addOperation(
                new ForOp(
                    LOC,
                    innerInit.getValue(),
                    innerLower.getValue(),
                    innerUpper.getValue(),
                    innerStep.getValue()));

    // Print both induction variables in inner loop
    innerFor
        .getRegion()
        .getEntryBlock()
        .addOperation(new PrintOp(LOC, outerFor.getInductionValue(), innerFor.getInductionValue()));
    innerFor.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    outerFor.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void forOpWithComplexBody() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(LOC, 10), 0);
    var step = funcOp.addOperation(new ConstantOp(LOC, 2), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                LOC,
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    // Complex body with multiple operations
    var text =
        forOp.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Loop iteration: "));
    forOp
        .getRegion()
        .getEntryBlock()
        .addOperation(new PrintOp(LOC, text.getValue(), forOp.getInductionValue()));

    // Nested scope inside loop
    ScopeOp innerScope = forOp.getRegion().getEntryBlock().addOperation(new ScopeOp(LOC));
    var scopeConst =
        innerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Inside scope"));
    innerScope.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, scopeConst.getValue()));
    innerScope.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void forOpMissingTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(LOC, 10), 0);
    var step = funcOp.addOperation(new ConstantOp(LOC, 1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                LOC,
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, forOp.getInductionValue()));
    // Missing ContinueOp

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  // ===================== Mixed SCF Operations Tests =====================

  @Test
  public void ifInsideScope() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(LOC), 0);

    var condOp = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, true));
    IfOp ifOp =
        scopeOp.getRegion().getEntryBlock().addOperation(new IfOp(LOC, condOp.getValue(), false));

    var thenConst =
        ifOp.getThenRegion()
            .getEntryBlock()
            .addOperation(new ConstantOp(LOC, "Inside if in scope"));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, thenConst.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void forInsideIf() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(LOC, condOp.getValue(), false), 0);

    // For loop inside if
    var initValue = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 0));
    var lowerBound = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 0));
    var upperBound = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 5));
    var step = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 1));

    ForOp forOp =
        ifOp.getThenRegion()
            .getEntryBlock()
            .addOperation(
                new ForOp(
                    LOC,
                    initValue.getValue(),
                    lowerBound.getValue(),
                    upperBound.getValue(),
                    step.getValue()));

    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, forOp.getInductionValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void complexNestedScfOperations() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    // Scope containing a for loop
    ScopeOp outerScope = funcOp.addOperation(new ScopeOp(LOC), 0);

    var initValue = outerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 0));
    var lowerBound = outerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 0));
    var upperBound = outerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 3));
    var step = outerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 1));

    ForOp forOp =
        outerScope
            .getRegion()
            .getEntryBlock()
            .addOperation(
                new ForOp(
                    LOC,
                    initValue.getValue(),
                    lowerBound.getValue(),
                    upperBound.getValue(),
                    step.getValue()));

    // If inside for
    var condOp = forOp.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, true));
    IfOp ifOp =
        forOp.getRegion().getEntryBlock().addOperation(new IfOp(LOC, condOp.getValue(), true));

    // Scope inside if then
    ScopeOp thenScope = ifOp.getThenRegion().getEntryBlock().addOperation(new ScopeOp(LOC));
    var thenText =
        thenScope.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Then scope"));
    thenScope
        .getRegion()
        .getEntryBlock()
        .addOperation(new PrintOp(LOC, thenText.getValue(), forOp.getInductionValue()));
    thenScope.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // Scope inside if else
    ScopeOp elseScope = ifOp.getElseRegion().get().getEntryBlock().addOperation(new ScopeOp(LOC));
    var elseText =
        elseScope.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Else scope"));
    elseScope.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, elseText.getValue()));
    elseScope.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new ContinueOp(LOC));

    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    outerScope.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void multipleSequentialScfOps() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    // First scope
    ScopeOp scope1 = funcOp.addOperation(new ScopeOp(LOC), 0);
    var const1 = scope1.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Scope 1"));
    scope1.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, const1.getValue()));
    scope1.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // If statement
    var condOp = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(LOC, condOp.getValue(), false), 0);
    var ifConst =
        ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "If block"));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, ifConst.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // For loop
    var initValue = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(LOC, 2), 0);
    var step = funcOp.addOperation(new ConstantOp(LOC, 1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                LOC,
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);
    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, forOp.getInductionValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // Second scope
    ScopeOp scope2 = funcOp.addOperation(new ScopeOp(LOC), 0);
    var const2 = scope2.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Scope 2"));
    scope2.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, const2.getValue()));
    scope2.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  // ===================== Value Visibility Tests =====================

  @Test
  public void valueDefinedInScopeNotVisibleOutside() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(LOC), 0);
    var innerValue = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 42));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, innerValue.getValue()));
    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // Try to use value defined inside scope - this should fail
    funcOp.addOperation(new PrintOp(LOC, innerValue.getValue()), 0);
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void valueDefinedInIfThenNotVisibleOutside() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(LOC, condOp.getValue(), false), 0);

    var thenValue = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, 100));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, thenValue.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // Try to use value defined in then block - this should fail
    funcOp.addOperation(new PrintOp(LOC, thenValue.getValue()), 0);
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void valueDefinedInForLoopNotVisibleOutside() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(LOC, 5), 0);
    var step = funcOp.addOperation(new ConstantOp(LOC, 1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                LOC,
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    var loopValue =
        forOp.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Inside loop"));
    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, loopValue.getValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // Try to use value defined inside loop - this should fail
    funcOp.addOperation(new PrintOp(LOC, loopValue.getValue()), 0);
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void inductionVariableNotVisibleOutsideLoop() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(LOC, 5), 0);
    var step = funcOp.addOperation(new ConstantOp(LOC, 1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                LOC,
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, forOp.getInductionValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    // Try to use induction variable outside loop - this should fail
    funcOp.addOperation(new PrintOp(LOC, forOp.getInductionValue()), 0);
    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  // ===================== Edge Case Tests =====================

  @Test
  public void emptyScopeOp() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(LOC), 0);
    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void emptyIfThenBranch() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(LOC, condOp.getValue(), false), 0);

    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void emptyIfElseBranch() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(LOC, true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(LOC, condOp.getValue(), true), 0);

    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void emptyForLoopBody() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(LOC, 0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(LOC, 5), 0);
    var step = funcOp.addOperation(new ConstantOp(LOC, 1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                LOC,
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void deeplyNestedScopes() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    // Create 5 levels of nested scopes
    ScopeOp scope1 = funcOp.addOperation(new ScopeOp(LOC), 0);
    ScopeOp scope2 = scope1.getRegion().getEntryBlock().addOperation(new ScopeOp(LOC));
    ScopeOp scope3 = scope2.getRegion().getEntryBlock().addOperation(new ScopeOp(LOC));
    ScopeOp scope4 = scope3.getRegion().getEntryBlock().addOperation(new ScopeOp(LOC));
    ScopeOp scope5 = scope4.getRegion().getEntryBlock().addOperation(new ScopeOp(LOC));

    var deepValue = scope5.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Deep"));
    scope5.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, deepValue.getValue()));
    scope5.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    scope4.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    scope3.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    scope2.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));
    scope1.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    funcOp.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void scfOpsInMultipleFunctions() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    // Create helper function with SCF ops
    FuncOp helperFunc =
        programOp.addOperation(new FuncOp(LOC, "helper", new FuncType(List.of(), IntegerT.INT32)));

    var initValue = helperFunc.addOperation(new ConstantOp(LOC, 0), 0);
    var lowerBound = helperFunc.addOperation(new ConstantOp(LOC, 0), 0);
    var upperBound = helperFunc.addOperation(new ConstantOp(LOC, 3), 0);
    var step = helperFunc.addOperation(new ConstantOp(LOC, 1), 0);

    ForOp forOp =
        helperFunc.addOperation(
            new ForOp(
                LOC,
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);
    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, forOp.getInductionValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    var returnValue = helperFunc.addOperation(new ConstantOp(LOC, 42), 0);
    helperFunc.addOperation(new ReturnOp(LOC, returnValue.getValue()), 0);

    // Main function with scope
    ScopeOp mainScope = mainFunc.addOperation(new ScopeOp(LOC), 0);
    var text =
        mainScope.getRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "Main scope"));
    mainScope.getRegion().getEntryBlock().addOperation(new PrintOp(LOC, text.getValue()));
    mainScope.getRegion().getEntryBlock().addOperation(new ContinueOp(LOC));

    mainFunc.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }
}

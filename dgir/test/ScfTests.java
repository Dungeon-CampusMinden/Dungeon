import static org.junit.jupiter.api.Assertions.*;

import core.Dialect;
import core.serialization.Utils;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.builtin.types.IntegerT;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.func.types.FuncType;
import dialect.io.PrintOp;
import dialect.scf.*;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * The test cases for the SCF dialect. These are mostly focused on testing the control flow and
 * region handling of the dialect, as well as the basic functionality of the ScopeOp. It also test
 * that reaching definitions hold in complex cases and some negative test cases.
 */
public class ScfTests {
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

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(), 0);
    var constOp = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(42));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(constOp.getValue()));
    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void nestedScopeOps() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp outerScope = funcOp.addOperation(new ScopeOp(), 0);
    var outerConst = outerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(10));

    ScopeOp innerScope = outerScope.getRegion().getEntryBlock().addOperation(new ScopeOp());
    var innerConst = innerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(20));
    // Inner scope can use values from outer scope
    innerScope.getRegion().getEntryBlock().addOperation(new PrintOp(outerConst.getValue()));
    innerScope.getRegion().getEntryBlock().addOperation(new PrintOp(innerConst.getValue()));
    innerScope.getRegion().getEntryBlock().addOperation(new ContinueOp());

    outerScope.getRegion().getEntryBlock().addOperation(new ContinueOp());
    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void scopeOpWithMultipleOperations() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(), 0);
    var const1 = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(1));
    var const2 = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(2));
    var const3 = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(3));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(const1.getValue()));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(const2.getValue()));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(const3.getValue()));
    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void scopeOpMissingTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(), 0);
    var constOp = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(42));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(constOp.getValue()));
    // Missing ContinueOp

    funcOp.addOperation(new ReturnOp(), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  // ===================== IfOp Tests =====================

  @Test
  public void simpleIfOpWithThen() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(condOp.getValue(), false), 0);

    var thenConst = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(42));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(thenConst.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void ifOpWithThenAndElse() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(condOp.getValue(), true), 0);

    // Then branch
    var thenConst =
        ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp("Then branch"));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(thenConst.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    // Else branch
    var elseConst =
        ifOp.getElseRegion().get().getEntryBlock().addOperation(new ConstantOp("Else branch"));
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new PrintOp(elseConst.getValue()));
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void nestedIfOps() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var outerCond = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp outerIf = funcOp.addOperation(new IfOp(outerCond.getValue(), false), 0);

    // Nested if inside outer then
    var innerCond = outerIf.getThenRegion().getEntryBlock().addOperation(new ConstantOp(false));
    IfOp innerIf =
        outerIf.getThenRegion().getEntryBlock().addOperation(new IfOp(innerCond.getValue(), true));

    // Inner then
    var innerThenConst =
        innerIf.getThenRegion().getEntryBlock().addOperation(new ConstantOp("Inner then"));
    innerIf.getThenRegion().getEntryBlock().addOperation(new PrintOp(innerThenConst.getValue()));
    innerIf.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    // Inner else
    var innerElseConst =
        innerIf.getElseRegion().get().getEntryBlock().addOperation(new ConstantOp("Inner else"));
    innerIf
        .getElseRegion()
        .get()
        .getEntryBlock()
        .addOperation(new PrintOp(innerElseConst.getValue()));
    innerIf.getElseRegion().get().getEntryBlock().addOperation(new ContinueOp());

    outerIf.getThenRegion().getEntryBlock().addOperation(new ContinueOp());
    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void ifOpWithValueFromOuterScope() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var outerValue = funcOp.addOperation(new ConstantOp(100), 0);
    var condOp = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(condOp.getValue(), true), 0);

    // Both branches should be able to use the outer value
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(outerValue.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    ifOp.getElseRegion().get().getEntryBlock().addOperation(new PrintOp(outerValue.getValue()));
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void ifOpMissingThenTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(condOp.getValue(), false), 0);

    var thenConst = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(42));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(thenConst.getValue()));
    // Missing ContinueOp

    funcOp.addOperation(new ReturnOp(), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void ifOpMissingElseTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(condOp.getValue(), true), 0);

    // Then branch is OK
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    // Else branch missing terminator
    var elseConst = ifOp.getElseRegion().get().getEntryBlock().addOperation(new ConstantOp(42));
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new PrintOp(elseConst.getValue()));
    // Missing ContinueOp

    funcOp.addOperation(new ReturnOp(), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  // ===================== ForOp Tests =====================

  @Test
  public void simpleForOp() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(10), 0);
    var step = funcOp.addOperation(new ConstantOp(1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    // Use the induction variable
    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(forOp.getInductionValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void nestedForOps() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    // Outer loop
    var outerInit = funcOp.addOperation(new ConstantOp(0), 0);
    var outerLower = funcOp.addOperation(new ConstantOp(0), 0);
    var outerUpper = funcOp.addOperation(new ConstantOp(5), 0);
    var outerStep = funcOp.addOperation(new ConstantOp(1), 0);

    ForOp outerFor =
        funcOp.addOperation(
            new ForOp(
                outerInit.getValue(),
                outerLower.getValue(),
                outerUpper.getValue(),
                outerStep.getValue()),
            0);

    // Inner loop
    var innerInit = outerFor.getRegion().getEntryBlock().addOperation(new ConstantOp(0));
    var innerLower = outerFor.getRegion().getEntryBlock().addOperation(new ConstantOp(0));
    var innerUpper = outerFor.getRegion().getEntryBlock().addOperation(new ConstantOp(3));
    var innerStep = outerFor.getRegion().getEntryBlock().addOperation(new ConstantOp(1));

    ForOp innerFor =
        outerFor
            .getRegion()
            .getEntryBlock()
            .addOperation(
                new ForOp(
                    innerInit.getValue(),
                    innerLower.getValue(),
                    innerUpper.getValue(),
                    innerStep.getValue()));

    // Print both induction variables in inner loop
    innerFor
        .getRegion()
        .getEntryBlock()
        .addOperation(new PrintOp(outerFor.getInductionValue(), innerFor.getInductionValue()));
    innerFor.getRegion().getEntryBlock().addOperation(new ContinueOp());

    outerFor.getRegion().getEntryBlock().addOperation(new ContinueOp());
    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void forOpWithComplexBody() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(10), 0);
    var step = funcOp.addOperation(new ConstantOp(2), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    // Complex body with multiple operations
    var text = forOp.getRegion().getEntryBlock().addOperation(new ConstantOp("Loop iteration: "));
    forOp
        .getRegion()
        .getEntryBlock()
        .addOperation(new PrintOp(text.getValue(), forOp.getInductionValue()));

    // Nested scope inside loop
    ScopeOp innerScope = forOp.getRegion().getEntryBlock().addOperation(new ScopeOp());
    var scopeConst =
        innerScope.getRegion().getEntryBlock().addOperation(new ConstantOp("Inside scope"));
    innerScope.getRegion().getEntryBlock().addOperation(new PrintOp(scopeConst.getValue()));
    innerScope.getRegion().getEntryBlock().addOperation(new ContinueOp());

    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp());
    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void forOpMissingTerminator() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(10), 0);
    var step = funcOp.addOperation(new ConstantOp(1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(forOp.getInductionValue()));
    // Missing ContinueOp

    funcOp.addOperation(new ReturnOp(), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  // ===================== Mixed SCF Operations Tests =====================

  @Test
  public void ifInsideScope() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(), 0);

    var condOp = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(true));
    IfOp ifOp =
        scopeOp.getRegion().getEntryBlock().addOperation(new IfOp(condOp.getValue(), false));

    var thenConst =
        ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp("Inside if in scope"));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(thenConst.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp());
    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void forInsideIf() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(condOp.getValue(), false), 0);

    // For loop inside if
    var initValue = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(0));
    var lowerBound = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(0));
    var upperBound = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(5));
    var step = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(1));

    ForOp forOp =
        ifOp.getThenRegion()
            .getEntryBlock()
            .addOperation(
                new ForOp(
                    initValue.getValue(),
                    lowerBound.getValue(),
                    upperBound.getValue(),
                    step.getValue()));

    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(forOp.getInductionValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());
    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void complexNestedScfOperations() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    // Scope containing a for loop
    ScopeOp outerScope = funcOp.addOperation(new ScopeOp(), 0);

    var initValue = outerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(0));
    var lowerBound = outerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(0));
    var upperBound = outerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(3));
    var step = outerScope.getRegion().getEntryBlock().addOperation(new ConstantOp(1));

    ForOp forOp =
        outerScope
            .getRegion()
            .getEntryBlock()
            .addOperation(
                new ForOp(
                    initValue.getValue(),
                    lowerBound.getValue(),
                    upperBound.getValue(),
                    step.getValue()));

    // If inside for
    var condOp = forOp.getRegion().getEntryBlock().addOperation(new ConstantOp(true));
    IfOp ifOp = forOp.getRegion().getEntryBlock().addOperation(new IfOp(condOp.getValue(), true));

    // Scope inside if then
    ScopeOp thenScope = ifOp.getThenRegion().getEntryBlock().addOperation(new ScopeOp());
    var thenText = thenScope.getRegion().getEntryBlock().addOperation(new ConstantOp("Then scope"));
    thenScope
        .getRegion()
        .getEntryBlock()
        .addOperation(new PrintOp(thenText.getValue(), forOp.getInductionValue()));
    thenScope.getRegion().getEntryBlock().addOperation(new ContinueOp());
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    // Scope inside if else
    ScopeOp elseScope = ifOp.getElseRegion().get().getEntryBlock().addOperation(new ScopeOp());
    var elseText = elseScope.getRegion().getEntryBlock().addOperation(new ConstantOp("Else scope"));
    elseScope.getRegion().getEntryBlock().addOperation(new PrintOp(elseText.getValue()));
    elseScope.getRegion().getEntryBlock().addOperation(new ContinueOp());
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new ContinueOp());

    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp());
    outerScope.getRegion().getEntryBlock().addOperation(new ContinueOp());
    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void multipleSequentialScfOps() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    // First scope
    ScopeOp scope1 = funcOp.addOperation(new ScopeOp(), 0);
    var const1 = scope1.getRegion().getEntryBlock().addOperation(new ConstantOp("Scope 1"));
    scope1.getRegion().getEntryBlock().addOperation(new PrintOp(const1.getValue()));
    scope1.getRegion().getEntryBlock().addOperation(new ContinueOp());

    // If statement
    var condOp = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(condOp.getValue(), false), 0);
    var ifConst = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp("If block"));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(ifConst.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    // For loop
    var initValue = funcOp.addOperation(new ConstantOp(0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(2), 0);
    var step = funcOp.addOperation(new ConstantOp(1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);
    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(forOp.getInductionValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    // Second scope
    ScopeOp scope2 = funcOp.addOperation(new ScopeOp(), 0);
    var const2 = scope2.getRegion().getEntryBlock().addOperation(new ConstantOp("Scope 2"));
    scope2.getRegion().getEntryBlock().addOperation(new PrintOp(const2.getValue()));
    scope2.getRegion().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  // ===================== Value Visibility Tests =====================

  @Test
  public void valueDefinedInScopeNotVisibleOutside() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(), 0);
    var innerValue = scopeOp.getRegion().getEntryBlock().addOperation(new ConstantOp(42));
    scopeOp.getRegion().getEntryBlock().addOperation(new PrintOp(innerValue.getValue()));
    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    // Try to use value defined inside scope - this should fail
    funcOp.addOperation(new PrintOp(innerValue.getValue()), 0);
    funcOp.addOperation(new ReturnOp(), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void valueDefinedInIfThenNotVisibleOutside() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(condOp.getValue(), false), 0);

    var thenValue = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(100));
    ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(thenValue.getValue()));
    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    // Try to use value defined in then block - this should fail
    funcOp.addOperation(new PrintOp(thenValue.getValue()), 0);
    funcOp.addOperation(new ReturnOp(), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void valueDefinedInForLoopNotVisibleOutside() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(5), 0);
    var step = funcOp.addOperation(new ConstantOp(1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    var loopValue = forOp.getRegion().getEntryBlock().addOperation(new ConstantOp("Inside loop"));
    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(loopValue.getValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    // Try to use value defined inside loop - this should fail
    funcOp.addOperation(new PrintOp(loopValue.getValue()), 0);
    funcOp.addOperation(new ReturnOp(), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void inductionVariableNotVisibleOutsideLoop() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(5), 0);
    var step = funcOp.addOperation(new ConstantOp(1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(forOp.getInductionValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    // Try to use induction variable outside loop - this should fail
    funcOp.addOperation(new PrintOp(forOp.getInductionValue()), 0);
    funcOp.addOperation(new ReturnOp(), 0);

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  // ===================== Edge Case Tests =====================

  @Test
  public void emptyScopeOp() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    ScopeOp scopeOp = funcOp.addOperation(new ScopeOp(), 0);
    scopeOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void emptyIfThenBranch() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(condOp.getValue(), false), 0);

    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void emptyIfElseBranch() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var condOp = funcOp.addOperation(new ConstantOp(true), 0);
    IfOp ifOp = funcOp.addOperation(new IfOp(condOp.getValue(), true), 0);

    ifOp.getThenRegion().getEntryBlock().addOperation(new ContinueOp());
    ifOp.getElseRegion().get().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void emptyForLoopBody() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var initValue = funcOp.addOperation(new ConstantOp(0), 0);
    var lowerBound = funcOp.addOperation(new ConstantOp(0), 0);
    var upperBound = funcOp.addOperation(new ConstantOp(5), 0);
    var step = funcOp.addOperation(new ConstantOp(1), 0);

    ForOp forOp =
        funcOp.addOperation(
            new ForOp(
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);

    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void deeplyNestedScopes() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    // Create 5 levels of nested scopes
    ScopeOp scope1 = funcOp.addOperation(new ScopeOp(), 0);
    ScopeOp scope2 = scope1.getRegion().getEntryBlock().addOperation(new ScopeOp());
    ScopeOp scope3 = scope2.getRegion().getEntryBlock().addOperation(new ScopeOp());
    ScopeOp scope4 = scope3.getRegion().getEntryBlock().addOperation(new ScopeOp());
    ScopeOp scope5 = scope4.getRegion().getEntryBlock().addOperation(new ScopeOp());

    var deepValue = scope5.getRegion().getEntryBlock().addOperation(new ConstantOp("Deep"));
    scope5.getRegion().getEntryBlock().addOperation(new PrintOp(deepValue.getValue()));
    scope5.getRegion().getEntryBlock().addOperation(new ContinueOp());

    scope4.getRegion().getEntryBlock().addOperation(new ContinueOp());
    scope3.getRegion().getEntryBlock().addOperation(new ContinueOp());
    scope2.getRegion().getEntryBlock().addOperation(new ContinueOp());
    scope1.getRegion().getEntryBlock().addOperation(new ContinueOp());

    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void scfOpsInMultipleFunctions() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp mainFunc = entry.getRight();

    // Create helper function with SCF ops
    FuncOp helperFunc =
        programOp.addOperation(new FuncOp("helper", new FuncType(List.of(), IntegerT.INT32)));

    var initValue = helperFunc.addOperation(new ConstantOp(0), 0);
    var lowerBound = helperFunc.addOperation(new ConstantOp(0), 0);
    var upperBound = helperFunc.addOperation(new ConstantOp(3), 0);
    var step = helperFunc.addOperation(new ConstantOp(1), 0);

    ForOp forOp =
        helperFunc.addOperation(
            new ForOp(
                initValue.getValue(),
                lowerBound.getValue(),
                upperBound.getValue(),
                step.getValue()),
            0);
    forOp.getRegion().getEntryBlock().addOperation(new PrintOp(forOp.getInductionValue()));
    forOp.getRegion().getEntryBlock().addOperation(new ContinueOp());

    var returnValue = helperFunc.addOperation(new ConstantOp(42), 0);
    helperFunc.addOperation(new ReturnOp(returnValue.getValue()), 0);

    // Main function with scope
    ScopeOp mainScope = mainFunc.addOperation(new ScopeOp(), 0);
    var text = mainScope.getRegion().getEntryBlock().addOperation(new ConstantOp("Main scope"));
    mainScope.getRegion().getEntryBlock().addOperation(new PrintOp(text.getValue()));
    mainScope.getRegion().getEntryBlock().addOperation(new ContinueOp());

    mainFunc.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }
}

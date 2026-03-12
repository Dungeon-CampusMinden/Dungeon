import dgir.core.debug.Location;
import org.junit.jupiter.api.Test;

import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.func.FuncOps.FuncOp;
import static dgir.dialect.func.FuncOps.ReturnOp;
import static dgir.dialect.io.IoOps.PrintOp;
import static dgir.dialect.scf.ScfOps.*;

/**
 * VM-level execution tests for all SCF runners: ForRunner, ContinueRunner, BreakRunner, IfRunner,
 * ScopeRunner.
 *
 * <p>Every test builds a dgir program, executes it through the VM, and asserts on the captured
 * stdout produced by PrintOp/PrintRunner.
 */
public class ScfTest extends VmTestBase {
  static final Location LOC = Location.UNKNOWN;
  /** Probe: for(i=0; i<3; i++) print(i) -- verify the actual output produced. */
  @Test
  void basicForLoopOutput() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));
    {
      var init = mainOp.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = mainOp.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = mainOp.addOperation(new ConstantOp(LOC, 3), 0);
      var step = mainOp.addOperation(new ConstantOp(LOC, 1), 0);
      var format = mainOp.addOperation(new ConstantOp(LOC, "%d\n"), 0);

      ForOp forOp =
          mainOp.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      {
        forOp
            .getEntryBlock()
            .addOperation(new PrintOp(LOC, format.getResult(), forOp.getInductionValue()));
        forOp.getEntryBlock().addOperation(new ContinueOp(LOC));
      }

      mainOp.addOperation(new ReturnOp(LOC), 0);
    }

    runProgram(programOp, "0\n1\n2\n");
  }

  // =========================================================================
  // ForRunner + ContinueRunner
  // =========================================================================

  /** for(i=0; i<3; i++) print(i) → "0\n1\n2\n" */
  @Test
  void forLoop_basicCounting() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 3), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);
      var fmt = main.addOperation(new ConstantOp(LOC, "%d\n"), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      forOp
          .getEntryBlock()
          .addOperation(new PrintOp(LOC, fmt.getResult(), forOp.getInductionValue()));
      forOp.getEntryBlock().addOperation(new ContinueOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "0\n1\n2\n");
  }

  /** Loop with step=2: for(i=0; i<6; i+=2) print(i) → "0\n2\n4\n" */
  @Test
  void forLoop_stepGreaterThanOne() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 6), 0);
      var step = main.addOperation(new ConstantOp(LOC, 2), 0);
      var fmt = main.addOperation(new ConstantOp(LOC, "%d\n"), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      forOp
          .getEntryBlock()
          .addOperation(new PrintOp(LOC, fmt.getResult(), forOp.getInductionValue()));
      forOp.getEntryBlock().addOperation(new ContinueOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "0\n2\n4\n");
  }

  /** init >= upper → loop body never executes, no output. */
  @Test
  void forLoop_zeroIterations_initEqualsUpper() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 5), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 5), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);
      var fmt = main.addOperation(new ConstantOp(LOC, "%d\n"), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      forOp
          .getEntryBlock()
          .addOperation(new PrintOp(LOC, fmt.getResult(), forOp.getInductionValue()));
      forOp.getEntryBlock().addOperation(new ContinueOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "");
  }

  /** init > upper → loop body never executes. */
  @Test
  void forLoop_zeroIterations_initAboveUpper() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 10), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 5), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);
      var fmt = main.addOperation(new ConstantOp(LOC, "%d\n"), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      forOp
          .getEntryBlock()
          .addOperation(new PrintOp(LOC, fmt.getResult(), forOp.getInductionValue()));
      forOp.getEntryBlock().addOperation(new ContinueOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "");
  }

  /** init < lowerBound → loop body never executes (init not in [lower, upper)). */
  @Test
  void forLoop_zeroIterations_initBelowLower() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 3), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 6), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);
      var fmt = main.addOperation(new ConstantOp(LOC, "%d\n"), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      forOp
          .getEntryBlock()
          .addOperation(new PrintOp(LOC, fmt.getResult(), forOp.getInductionValue()));
      forOp.getEntryBlock().addOperation(new ContinueOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "");
  }

  /** for(i=1; i<4; i++) — non-zero start — prints "1\n2\n3\n". */
  @Test
  void forLoop_nonZeroInitialValue() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 1), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 1), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 4), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);
      var fmt = main.addOperation(new ConstantOp(LOC, "%d\n"), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      forOp
          .getEntryBlock()
          .addOperation(new PrintOp(LOC, fmt.getResult(), forOp.getInductionValue()));
      forOp.getEntryBlock().addOperation(new ContinueOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "1\n2\n3\n");
  }

  // =========================================================================
  // BreakRunner
  // =========================================================================

  /**
   * Break on first iteration (i==0): loop runs once, breaks, no more output. for(i=0; i<5; i++) {
   * print(i); break; } → "0\n"
   */
  @Test
  void forLoop_breakOnFirstIteration() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 5), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);
      var fmt = main.addOperation(new ConstantOp(LOC, "%d\n"), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      // Always print then break — only first iteration ever runs.
      forOp
          .getEntryBlock()
          .addOperation(new PrintOp(LOC, fmt.getResult(), forOp.getInductionValue()));
      forOp.getEntryBlock().addOperation(new BreakOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "0\n");
  }

  /**
   * Print "after" after a loop that was broken to confirm execution continues past the loop.
   * for(i=0; i<3; i++) { break; } print("after\n")
   */
  @Test
  void forLoop_breakThenContinueAfterLoop() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 3), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);
      var after = main.addOperation(new ConstantOp(LOC, "after\n"), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      forOp.getEntryBlock().addOperation(new BreakOp(LOC));

      main.addOperation(new PrintOp(LOC, after.getResult()), 0);
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "after\n");
  }

  // =========================================================================
  // IfRunner + ContinueRunner in non-for context
  // =========================================================================

  /** if(true) { print("yes\n") } → "yes\n" */
  @Test
  void ifRunner_trueBranchExecuted() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var cond = main.addOperation(new ConstantOp(LOC, true), 0);
      IfOp ifOp = main.addOperation(new IfOp(LOC, cond.getResult(), false), 0);

      var yes = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "yes\n"));
      ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, yes.getResult()));
      ifOp.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "yes\n");
  }

  /** if(false) { print("yes\n") } → "" (else-less if, false condition) */
  @Test
  void ifRunner_falseConditionNoElse_noOutput() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var cond = main.addOperation(new ConstantOp(LOC, false), 0);
      IfOp ifOp = main.addOperation(new IfOp(LOC, cond.getResult(), false), 0);

      var yes = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "yes\n"));
      ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, yes.getResult()));
      ifOp.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "");
  }

  /** if(true) { print("then\n") } else { print("else\n") } → "then\n" */
  @Test
  void ifRunner_trueBranch_withElse() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var cond = main.addOperation(new ConstantOp(LOC, true), 0);
      IfOp ifOp = main.addOperation(new IfOp(LOC, cond.getResult(), true), 0);

      var thenMsg = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "then\n"));
      ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, thenMsg.getResult()));
      ifOp.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

      var elseMsg =
          ifOp.getElseRegion().get().getEntryBlock().addOperation(new ConstantOp(LOC, "else\n"));
      ifOp.getElseRegion()
          .get()
          .getEntryBlock()
          .addOperation(new PrintOp(LOC, elseMsg.getResult()));
      ifOp.getElseRegion().get().getEntryBlock().addOperation(new EndOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "then\n");
  }

  /** if(false) { print("then\n") } else { print("else\n") } → "else\n" */
  @Test
  void ifRunner_falseBranch_withElse() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var cond = main.addOperation(new ConstantOp(LOC, false), 0);
      IfOp ifOp = main.addOperation(new IfOp(LOC, cond.getResult(), true), 0);

      var thenMsg = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "then\n"));
      ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, thenMsg.getResult()));
      ifOp.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

      var elseMsg =
          ifOp.getElseRegion().get().getEntryBlock().addOperation(new ConstantOp(LOC, "else\n"));
      ifOp.getElseRegion()
          .get()
          .getEntryBlock()
          .addOperation(new PrintOp(LOC, elseMsg.getResult()));
      ifOp.getElseRegion().get().getEntryBlock().addOperation(new EndOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "else\n");
  }

  /** Code after an if-without-else still runs when condition is false. */
  @Test
  void ifRunner_executionContinuesAfterSkippedIf() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var cond = main.addOperation(new ConstantOp(LOC, false), 0);
      IfOp ifOp = main.addOperation(new IfOp(LOC, cond.getResult(), false), 0);

      var skip = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "skip\n"));
      ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, skip.getResult()));
      ifOp.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

      var after = main.addOperation(new ConstantOp(LOC, "after\n"), 0);
      main.addOperation(new PrintOp(LOC, after.getResult()), 0);
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "after\n");
  }

  // =========================================================================
  // ScopeRunner + ContinueRunner in scope context
  // =========================================================================

  /** Scope executes its body: print("scope\n") → "scope\n" */
  @Test
  void scopeRunner_basicExecution() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      ScopeOp scope = main.addOperation(new ScopeOp(LOC), 0);
      var msg = scope.getEntryBlock().addOperation(new ConstantOp(LOC, "scope\n"));
      scope.getEntryBlock().addOperation(new PrintOp(LOC, msg.getResult()));
      scope.getEntryBlock().addOperation(new EndOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "scope\n");
  }

  /** Code after a scope still executes. */
  @Test
  void scopeRunner_executionContinuesAfterScope() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      ScopeOp scope = main.addOperation(new ScopeOp(LOC), 0);
      var inMsg = scope.getEntryBlock().addOperation(new ConstantOp(LOC, "in\n"));
      scope.getEntryBlock().addOperation(new PrintOp(LOC, inMsg.getResult()));
      scope.getEntryBlock().addOperation(new EndOp(LOC));

      var afterMsg = main.addOperation(new ConstantOp(LOC, "out\n"), 0);
      main.addOperation(new PrintOp(LOC, afterMsg.getResult()), 0);
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "in\nout\n");
  }

  // =========================================================================
  // Nested constructs
  // =========================================================================

  /** Nested for loops: for(i=0;i<2;i++) for(j=0;j<3;j++) print(j) Expected: "0\n1\n2\n0\n1\n2\n" */
  @Test
  void nestedForLoops_innerCounterResets() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var outerInit = main.addOperation(new ConstantOp(LOC, 0), 0);
      var outerLower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var outerUpper = main.addOperation(new ConstantOp(LOC, 2), 0);
      var outerStep = main.addOperation(new ConstantOp(LOC, 1), 0);

      ForOp outer =
          main.addOperation(
              new ForOp(
                  LOC,
                  outerInit.getResult(),
                  outerLower.getResult(),
                  outerUpper.getResult(),
                  outerStep.getResult()),
              0);
      {
        var innerInit = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 0));
        var innerLower = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 0));
        var innerUpper = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 3));
        var innerStep = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 1));
        var fmt = outer.getEntryBlock().addOperation(new ConstantOp(LOC, "%d\n"));

        ForOp inner =
            outer
                .getEntryBlock()
                .addOperation(
                    new ForOp(
                        LOC,
                        innerInit.getResult(),
                        innerLower.getResult(),
                        innerUpper.getResult(),
                        innerStep.getResult()));
        inner
            .getEntryBlock()
            .addOperation(new PrintOp(LOC, fmt.getResult(), inner.getInductionValue()));
        inner.getEntryBlock().addOperation(new ContinueOp(LOC));

        outer.getEntryBlock().addOperation(new ContinueOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "0\n1\n2\n0\n1\n2\n");
  }

  /**
   * Nested for loops: print both induction variables. for(i=0;i<2;i++) for(j=0;j<2;j++) print(i, j)
   * Expected: "0 0\n0 1\n1 0\n1 1\n"
   */
  @Test
  void nestedForLoops_bothInductionVariables() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var outerInit = main.addOperation(new ConstantOp(LOC, 0), 0);
      var outerLower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var outerUpper = main.addOperation(new ConstantOp(LOC, 2), 0);
      var outerStep = main.addOperation(new ConstantOp(LOC, 1), 0);

      ForOp outer =
          main.addOperation(
              new ForOp(
                  LOC,
                  outerInit.getResult(),
                  outerLower.getResult(),
                  outerUpper.getResult(),
                  outerStep.getResult()),
              0);
      {
        var innerInit = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 0));
        var innerLower = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 0));
        var innerUpper = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 2));
        var innerStep = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 1));
        var fmt = outer.getEntryBlock().addOperation(new ConstantOp(LOC, "%d %d\n"));

        ForOp inner =
            outer
                .getEntryBlock()
                .addOperation(
                    new ForOp(
                        LOC,
                        innerInit.getResult(),
                        innerLower.getResult(),
                        innerUpper.getResult(),
                        innerStep.getResult()));
        inner
            .getEntryBlock()
            .addOperation(
                new PrintOp(
                    LOC, fmt.getResult(), outer.getInductionValue(), inner.getInductionValue()));
        inner.getEntryBlock().addOperation(new ContinueOp(LOC));

        outer.getEntryBlock().addOperation(new ContinueOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "0 0\n0 1\n1 0\n1 1\n");
  }

  /**
   * Break in inner loop does not affect the outer loop. for(i=0;i<2;i++) { for(j=0;j<3;j++) {
   * break; } print(i) } Expected: "0\n1\n"
   */
  @Test
  void nestedForLoops_breakInnerDoesNotAffectOuter() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var outerInit = main.addOperation(new ConstantOp(LOC, 0), 0);
      var outerLower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var outerUpper = main.addOperation(new ConstantOp(LOC, 2), 0);
      var outerStep = main.addOperation(new ConstantOp(LOC, 1), 0);
      var fmt = main.addOperation(new ConstantOp(LOC, "%d\n"), 0);

      ForOp outer =
          main.addOperation(
              new ForOp(
                  LOC,
                  outerInit.getResult(),
                  outerLower.getResult(),
                  outerUpper.getResult(),
                  outerStep.getResult()),
              0);
      {
        var innerInit = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 0));
        var innerLower = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 0));
        var innerUpper = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 3));
        var innerStep = outer.getEntryBlock().addOperation(new ConstantOp(LOC, 1));

        ForOp inner =
            outer
                .getEntryBlock()
                .addOperation(
                    new ForOp(
                        LOC,
                        innerInit.getResult(),
                        innerLower.getResult(),
                        innerUpper.getResult(),
                        innerStep.getResult()));
        inner.getEntryBlock().addOperation(new BreakOp(LOC));

        outer
            .getEntryBlock()
            .addOperation(new PrintOp(LOC, fmt.getResult(), outer.getInductionValue()));
        outer.getEntryBlock().addOperation(new ContinueOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "0\n1\n");
  }

  /**
   * If nested inside a for loop. for(i=0;i<4;i++) if(i==2) print("hit\n") else print("miss\n")
   *
   * <p>Since we have no comparison op, we use two separate runs with hard-coded conditions. Here:
   * condition is based on a constant — we verify the correct branch is taken each iteration by
   * using two adjacent fors with opposite hard-coded conditions.
   *
   * <p>Simpler approach: one loop, condition=true → all iterations take then-branch.
   * for(i=0;i<3;i++) if(true) print("T\n") else print("F\n") → "T\nT\nT\n"
   */
  @Test
  void forLoop_withIfInside_allTrue() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 3), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      {
        var cond = forOp.getEntryBlock().addOperation(new ConstantOp(LOC, true));
        IfOp ifOp = forOp.getEntryBlock().addOperation(new IfOp(LOC, cond.getResult(), true));

        var tMsg = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "T\n"));
        ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, tMsg.getResult()));
        ifOp.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

        var fMsg = ifOp.getElseRegion().orElseThrow().getEntryBlock().addOperation(new ConstantOp(LOC, "F\n"));
        ifOp.getElseRegion().get().getEntryBlock().addOperation(new PrintOp(LOC, fMsg.getResult()));
        ifOp.getElseRegion().get().getEntryBlock().addOperation(new EndOp(LOC));

        forOp.getEntryBlock().addOperation(new ContinueOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "T\nT\nT\n");
  }

  /** for(i=0;i<3;i++) if(false) print("T\n") else print("F\n") → "F\nF\nF\n" */
  @Test
  void forLoop_withIfInside_allFalse() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 3), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      {
        var cond = forOp.getEntryBlock().addOperation(new ConstantOp(LOC, false));
        IfOp ifOp = forOp.getEntryBlock().addOperation(new IfOp(LOC, cond.getResult(), true));

        var tMsg = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "T\n"));
        ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, tMsg.getResult()));
        ifOp.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

        var fMsg = ifOp.getElseRegion().get().getEntryBlock().addOperation(new ConstantOp(LOC, "F\n"));
        ifOp.getElseRegion().get().getEntryBlock().addOperation(new PrintOp(LOC, fMsg.getResult()));
        ifOp.getElseRegion().get().getEntryBlock().addOperation(new EndOp(LOC));

        forOp.getEntryBlock().addOperation(new ContinueOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "F\nF\nF\n");
  }

  /** Nested ifs: if(true) { if(true) print("inner\n") } → "inner\n" */
  @Test
  void nestedIfs_bothTrue() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var outerCond = main.addOperation(new ConstantOp(LOC, true), 0);
      IfOp outerIf = main.addOperation(new IfOp(LOC, outerCond.getResult(), false), 0);
      {
        var innerCond = outerIf.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, true));
        IfOp innerIf =
            outerIf
                .getThenRegion()
                .getEntryBlock()
                .addOperation(new IfOp(LOC, innerCond.getResult(), false));

        var msg = innerIf.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "inner\n"));
        innerIf.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, msg.getResult()));
        innerIf.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

        outerIf.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "inner\n");
  }

  /** Nested ifs: outer true, inner false → inner then-branch skipped, no output. */
  @Test
  void nestedIfs_outerTrueInnerFalse_noOutput() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var outerCond = main.addOperation(new ConstantOp(LOC, true), 0);
      IfOp outerIf = main.addOperation(new IfOp(LOC, outerCond.getResult(), false), 0);
      {
        var innerCond = outerIf.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, false));
        IfOp innerIf =
            outerIf
                .getThenRegion()
                .getEntryBlock()
                .addOperation(new IfOp(LOC, innerCond.getResult(), false));

        var msg = innerIf.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "skip\n"));
        innerIf.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, msg.getResult()));
        innerIf.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

        outerIf.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "");
  }

  /** Nested ifs: outer false → entire outer then-block skipped, including inner if. */
  @Test
  void nestedIfs_outerFalse_nothingExecutes() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var outerCond = main.addOperation(new ConstantOp(LOC, false), 0);
      IfOp outerIf = main.addOperation(new IfOp(LOC, outerCond.getResult(), false), 0);
      {
        var innerCond = outerIf.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, true));
        IfOp innerIf =
            outerIf
                .getThenRegion()
                .getEntryBlock()
                .addOperation(new IfOp(LOC, innerCond.getResult(), false));

        var msg = innerIf.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "skip\n"));
        innerIf.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, msg.getResult()));
        innerIf.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

        outerIf.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "");
  }

  /** Scope nested inside a for loop. for(i=0;i<2;i++) { scope { print("s\n") } } → "s\ns\n" */
  @Test
  void forLoop_withScopeInside() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 2), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      {
        ScopeOp scope = forOp.getEntryBlock().addOperation(new ScopeOp(LOC));
        var msg = scope.getEntryBlock().addOperation(new ConstantOp(LOC, "s\n"));
        scope.getEntryBlock().addOperation(new PrintOp(LOC, msg.getResult()));
        scope.getEntryBlock().addOperation(new EndOp(LOC));

        forOp.getEntryBlock().addOperation(new ContinueOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "s\ns\n");
  }

  /**
   * Scope nested inside a for loop: induction variable visible inside scope. for(i=0;i<3;i++) {
   * scope { print(i) } } → "0\n1\n2\n"
   */
  @Test
  void forLoop_withScopeInside_inductionVisibleInScope() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 3), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);
      var fmt = main.addOperation(new ConstantOp(LOC, "%d\n"), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      {
        ScopeOp scope = forOp.getEntryBlock().addOperation(new ScopeOp(LOC));
        scope
            .getEntryBlock()
            .addOperation(new PrintOp(LOC, fmt.getResult(), forOp.getInductionValue()));
        scope.getEntryBlock().addOperation(new EndOp(LOC));

        forOp.getEntryBlock().addOperation(new ContinueOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "0\n1\n2\n");
  }

  /** Nested scopes. scope { scope { print("inner\n") } print("outer\n") } → "inner\nouter\n" */
  @Test
  void nestedScopes() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      ScopeOp outer = main.addOperation(new ScopeOp(LOC), 0);
      {
        ScopeOp inner = outer.getEntryBlock().addOperation(new ScopeOp(LOC));
        var innerMsg = inner.getEntryBlock().addOperation(new ConstantOp(LOC, "inner\n"));
        inner.getEntryBlock().addOperation(new PrintOp(LOC, innerMsg.getResult()));
        inner.getEntryBlock().addOperation(new EndOp(LOC));

        var outerMsg = outer.getEntryBlock().addOperation(new ConstantOp(LOC, "outer\n"));
        outer.getEntryBlock().addOperation(new PrintOp(LOC, outerMsg.getResult()));
        outer.getEntryBlock().addOperation(new EndOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "inner\nouter\n");
  }

  /** If inside a scope. scope { if(true) print("hit\n") } → "hit\n" */
  @Test
  void scopeWithIfInside() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      ScopeOp scope = main.addOperation(new ScopeOp(LOC), 0);
      {
        var cond = scope.getEntryBlock().addOperation(new ConstantOp(LOC, true));
        IfOp ifOp = scope.getEntryBlock().addOperation(new IfOp(LOC, cond.getResult(), false));

        var msg = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "hit\n"));
        ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, msg.getResult()));
        ifOp.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

        scope.getEntryBlock().addOperation(new EndOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "hit\n");
  }

  /**
   * Three-level nesting: for → scope → if. for(i=0;i<2;i++) { scope { if(true) print("x\n") } } →
   * "x\nx\n"
   */
  @Test
  void tripleNesting_forScopeIf() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 2), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);

      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      {
        ScopeOp scope = forOp.getEntryBlock().addOperation(new ScopeOp(LOC));
        {
          var cond = scope.getEntryBlock().addOperation(new ConstantOp(LOC, true));
          IfOp ifOp = scope.getEntryBlock().addOperation(new IfOp(LOC, cond.getResult(), false));

          var msg = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "x\n"));
          ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, msg.getResult()));
          ifOp.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

          scope.getEntryBlock().addOperation(new EndOp(LOC));
        }
        forOp.getEntryBlock().addOperation(new ContinueOp(LOC));
      }
      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "x\nx\n");
  }

  /**
   * Multiple SCF constructs in sequence inside one function: scope { print("a\n") } if(true) {
   * print("b\n") } for(i=0;i<2;i++) { print("c\n") } Expected: "a\nb\nc\nc\n"
   */
  @Test
  void multipleScfConstructsInSequence() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    {
      // Scope
      ScopeOp scope = main.addOperation(new ScopeOp(LOC), 0);
      var aMsg = scope.getEntryBlock().addOperation(new ConstantOp(LOC, "a\n"));
      scope.getEntryBlock().addOperation(new PrintOp(LOC, aMsg.getResult()));
      scope.getEntryBlock().addOperation(new EndOp(LOC));

      // If
      var cond = main.addOperation(new ConstantOp(LOC, true), 0);
      IfOp ifOp = main.addOperation(new IfOp(LOC, cond.getResult(), false), 0);
      var bMsg = ifOp.getThenRegion().getEntryBlock().addOperation(new ConstantOp(LOC, "b\n"));
      ifOp.getThenRegion().getEntryBlock().addOperation(new PrintOp(LOC, bMsg.getResult()));
      ifOp.getThenRegion().getEntryBlock().addOperation(new EndOp(LOC));

      // For
      var init = main.addOperation(new ConstantOp(LOC, 0), 0);
      var lower = main.addOperation(new ConstantOp(LOC, 0), 0);
      var upper = main.addOperation(new ConstantOp(LOC, 2), 0);
      var step = main.addOperation(new ConstantOp(LOC, 1), 0);
      ForOp forOp =
          main.addOperation(
              new ForOp(
                  LOC, init.getResult(), lower.getResult(), upper.getResult(), step.getResult()),
              0);
      var cMsg = forOp.getEntryBlock().addOperation(new ConstantOp(LOC, "c\n"));
      forOp.getEntryBlock().addOperation(new PrintOp(LOC, cMsg.getResult()));
      forOp.getEntryBlock().addOperation(new ContinueOp(LOC));

      main.addOperation(new ReturnOp(LOC), 0);
    }
    runProgram(prog, "a\nb\nc\nc\n");
  }
}

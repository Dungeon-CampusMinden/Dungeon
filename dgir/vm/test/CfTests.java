import dgir.core.debug.Location;
import dgir.core.ir.Block;
import dgir.dialect.builtin.BuiltinTypes.IntegerT;
import dgir.vm.api.VM;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dgir.dialect.arith.ArithAttrs.BinModeAttr.BinMode.EQ;
import static dgir.dialect.arith.ArithOps.BinaryOp;
import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.cf.CfOps.*;
import static dgir.dialect.func.FuncOps.*;
import static dgir.dialect.func.FuncTypes.FuncType;
import static dgir.dialect.io.IoOps.PrintOp;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** VM-level execution tests for CF dialect runners (branch and conditional branch). */
public class CfTests extends VmTestBase {
  static final Location LOC = Location.UNKNOWN;

  // =========================================================================
  // BranchRunner (cf.br)
  // =========================================================================

  /** cf.br jumps unconditionally from entry to target block. */
  @Test
  void branch_unconditionalJump_executesTargetBlock() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));

    Block entry = main.getEntryBlock();
    Block target = main.addBlock(new Block());

    var before = entry.addOperation(new ConstantOp(LOC, "before\n"));
    entry.addOperation(new PrintOp(LOC, before.getValue()));
    entry.addOperation(new BranchOp(LOC, target));

    var after = target.addOperation(new ConstantOp(LOC, "after\n"));
    target.addOperation(new PrintOp(LOC, after.getValue()));
    target.addOperation(new ReturnOp(LOC));

    runProgram(prog, "before\nafter\n");
  }

  /** Multi-hop cf.br chain through three blocks preserves control-flow order. */
  @Test
  void branch_chainOfBlocks_executesInSequence() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));

    Block b0 = main.getEntryBlock();
    Block b1 = main.addBlock(new Block());
    Block b2 = main.addBlock(new Block());

    b0.addOperation(new PrintOp(LOC, b0.addOperation(new ConstantOp(LOC, "0\n")).getValue()));
    b0.addOperation(new BranchOp(LOC, b1));

    b1.addOperation(new PrintOp(LOC, b1.addOperation(new ConstantOp(LOC, "1\n")).getValue()));
    b1.addOperation(new BranchOp(LOC, b2));

    b2.addOperation(new PrintOp(LOC, b2.addOperation(new ConstantOp(LOC, "2\n")).getValue()));
    b2.addOperation(new ReturnOp(LOC));

    runProgram(prog, "0\n1\n2\n");
  }

  /** Negative: branching to a block without terminator must fail verification. */
  @Test
  void branch_targetWithoutTerminator_failsVerification() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));

    Block entry = main.getEntryBlock();
    Block invalid = main.addBlock(new Block());

    entry.addOperation(new BranchOp(LOC, invalid));
    invalid.addOperation(
        new PrintOp(LOC, invalid.addOperation(new ConstantOp(LOC, "x")).getValue()));

    assertThrows(AssertionError.class, () -> createVm(prog));
  }

  // =========================================================================
  // BranchCondRunner (cf.br_cond)
  // =========================================================================

  /** cf.br_cond with true condition takes the first successor block. */
  @Test
  void branchCond_trueCondition_takesTrueBlock() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));

    Block entry = main.getEntryBlock();
    Block yes = main.addBlock(new Block());
    Block no = main.addBlock(new Block());
    Block merge = main.addBlock(new Block());

    var cond = entry.addOperation(new ConstantOp(LOC, true));
    entry.addOperation(new BranchCondOp(LOC, cond.getValue(), yes, no));

    yes.addOperation(new PrintOp(LOC, yes.addOperation(new ConstantOp(LOC, "yes\n")).getValue()));
    yes.addOperation(new BranchOp(LOC, merge));

    no.addOperation(new PrintOp(LOC, no.addOperation(new ConstantOp(LOC, "no\n")).getValue()));
    no.addOperation(new BranchOp(LOC, merge));

    merge.addOperation(new ReturnOp(LOC));

    runProgram(prog, "yes\n");
  }

  /** cf.br_cond with false condition takes the second successor block. */
  @Test
  void branchCond_falseCondition_takesFalseBlock() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));

    Block entry = main.getEntryBlock();
    Block yes = main.addBlock(new Block());
    Block no = main.addBlock(new Block());
    Block merge = main.addBlock(new Block());

    var cond = entry.addOperation(new ConstantOp(LOC, false));
    entry.addOperation(new BranchCondOp(LOC, cond.getValue(), yes, no));

    yes.addOperation(new PrintOp(LOC, yes.addOperation(new ConstantOp(LOC, "yes\n")).getValue()));
    yes.addOperation(new BranchOp(LOC, merge));

    no.addOperation(new PrintOp(LOC, no.addOperation(new ConstantOp(LOC, "no\n")).getValue()));
    no.addOperation(new BranchOp(LOC, merge));

    merge.addOperation(new ReturnOp(LOC));

    runProgram(prog, "no\n");
  }

  /** Mixes arith+cf: compare two ints with arith.bin(eq), then branch on the bool result. */
  @Test
  void branchCond_conditionFromArithBinaryOp() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));

    Block entry = main.getEntryBlock();
    Block equal = main.addBlock(new Block());
    Block notEqual = main.addBlock(new Block());
    Block merge = main.addBlock(new Block());

    var lhs = entry.addOperation(new ConstantOp(LOC, 21));
    var rhs = entry.addOperation(new ConstantOp(LOC, 21));
    var eq = entry.addOperation(new BinaryOp(LOC, lhs.getValue(), rhs.getValue(), EQ));
    entry.addOperation(new BranchCondOp(LOC, eq.getResult(), equal, notEqual));

    equal.addOperation(
        new PrintOp(LOC, equal.addOperation(new ConstantOp(LOC, "equal\n")).getValue()));
    equal.addOperation(new BranchOp(LOC, merge));

    notEqual.addOperation(
        new PrintOp(LOC, notEqual.addOperation(new ConstantOp(LOC, "not-equal\n")).getValue()));
    notEqual.addOperation(new BranchOp(LOC, merge));

    merge.addOperation(new ReturnOp(LOC));

    runProgram(prog, "equal\n");
  }

  /** Mixes func+cf: condition is returned by a callee and consumed by cf.br_cond in caller. */
  @Test
  void branchCond_conditionFromFunctionCall() {
    ProgramOp prog = new ProgramOp(LOC);

    FuncOp condFn =
        prog.addOperation(new FuncOp(LOC, "cond", new FuncType(List.of(), IntegerT.BOOL)));
    var fnCond = condFn.addOperation(new ConstantOp(LOC, false), 0);
    condFn.addOperation(new ReturnOp(LOC, fnCond.getValue()), 0);

    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));
    Block entry = main.getEntryBlock();
    Block yes = main.addBlock(new Block());
    Block no = main.addBlock(new Block());
    Block merge = main.addBlock(new Block());

    var call = entry.addOperation(new CallOp(LOC, condFn));
    entry.addOperation(new BranchCondOp(LOC, call.getOutputValue().orElseThrow(), yes, no));

    yes.addOperation(new PrintOp(LOC, yes.addOperation(new ConstantOp(LOC, "T\n")).getValue()));
    yes.addOperation(new BranchOp(LOC, merge));

    no.addOperation(new PrintOp(LOC, no.addOperation(new ConstantOp(LOC, "F\n")).getValue()));
    no.addOperation(new BranchOp(LOC, merge));

    merge.addOperation(new ReturnOp(LOC));

    runProgram(prog, "F\n");
  }

  /** Negative: selected branch block without terminator should fail verification. */
  @Test
  void branchCond_selectedBlockWithoutTerminator_failsVerification() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));

    Block entry = main.getEntryBlock();
    Block bad = main.addBlock(new Block());
    Block good = main.addBlock(new Block());

    var cond = entry.addOperation(new ConstantOp(LOC, true));
    entry.addOperation(new BranchCondOp(LOC, cond.getValue(), bad, good));

    bad.addOperation(
        new PrintOp(LOC, bad.addOperation(new ConstantOp(LOC, "bad-path")).getValue()));
    good.addOperation(new ReturnOp(LOC));

    assertThrows(AssertionError.class, () -> createVm(prog));
  }

  // =========================================================================
  // AssertRunner / cf.assert
  // =========================================================================

  /** cf.assert(true) should continue execution and not abort the VM. */
  @Test
  void assert_trueCondition_continuesExecution() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));

    var cond = main.addOperation(new ConstantOp(LOC, true), 0);
    var ok = main.addOperation(new ConstantOp(LOC, "after-assert\n"), 0);
    main.addOperation(new AssertOp(LOC, cond.getValue()), 0);
    main.addOperation(new PrintOp(LOC, ok.getValue()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(prog, "after-assert\n");
  }

  /** cf.assert(false) without message should abort execution and skip following ops. */
  @Test
  void assert_falseConditionWithoutMessage_abortsExecution() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));

    var cond = main.addOperation(new ConstantOp(LOC, false), 0);
    var after = main.addOperation(new ConstantOp(LOC, "should-not-print\n"), 0);
    main.addOperation(new AssertOp(LOC, cond.getValue()), 0);
    main.addOperation(new PrintOp(LOC, after.getValue()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    VM vm = createVm(prog);
    assertFalse(vm.run(), "cf.assert(false) should abort the program");
    checkOutput("");
  }

  /** cf.assert(false, msg) aborts execution and still prevents later prints. */
  @Test
  void assert_falseConditionWithMessage_abortsExecution() {
    ProgramOp prog = new ProgramOp(LOC);
    FuncOp main = prog.addOperation(new FuncOp(LOC, "main"));

    var cond = main.addOperation(new ConstantOp(LOC, false), 0);
    var msg = main.addOperation(new ConstantOp(LOC, "assert failed"), 0);
    var after = main.addOperation(new ConstantOp(LOC, "never\n"), 0);
    main.addOperation(new AssertOp(LOC, cond.getValue(), msg.getValue()), 0);
    main.addOperation(new PrintOp(LOC, after.getValue()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    VM vm = createVm(prog);
    assertFalse(vm.run(), "cf.assert(false, msg) should abort the program");
    checkOutput("");
  }
}

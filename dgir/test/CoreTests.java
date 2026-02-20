import static org.junit.jupiter.api.Assertions.*;

import core.Dialect;
import core.ir.Block;
import core.serialization.Utils;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.cf.BranchCondOp;
import dialect.cf.BranchOp;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.io.PrintOp;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * These are test for checking the validity of the core IR and traits. These test are mainly there
 * to check if the structural analysis of the IR hold, especially reaching definitions and in that
 * context region visiblity, nesting and isolation.
 */
public class CoreTests {
  static boolean printResult = true;
  static boolean printDotGraph = true;
  static ObjectMapper mapper;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
    mapper = Utils.getMapper(true);
  }

  @Test
  public void reachingDefSameBlock() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();

    var constOp = funcOp.addOperation(new ConstantOp(42), 0);
    funcOp.addOperation(new PrintOp(constOp.getValue()), 0);
    funcOp.addOperation(new ReturnOp(), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void reachingDefSuccessorBlock() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();
    Block entryBlock = funcOp.getRegion().getEntryBlock();

    // Create a new block
    Block targetBlock = new Block();
    funcOp.getRegion().addBlock(targetBlock);

    // Entry block: define val, branch to target
    var constOp = entryBlock.addOperation(new ConstantOp(42));
    entryBlock.addOperation(new BranchOp(targetBlock));

    // Target block: use val, return
    targetBlock.addOperation(new PrintOp(constOp.getOutputValueThrowing()));
    targetBlock.addOperation(new ReturnOp());

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void reachingDefDominanceViolation() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();
    Block entryBlock = funcOp.getEntryBlock();

    Block leftBlock = funcOp.addBlock(new Block());
    Block rightBlock = funcOp.addBlock(new Block());
    Block mergeBlock = funcOp.addBlock(new Block());

    // Entry branches conditionally to left or right
    var cond =
        entryBlock.addOperation(
            new ConstantOp(
                true)); // Condition value, type doesn't matter for this test context usually, but
    // should be boolean-like ideally
    // Assuming ConstantOp(42) produces a valid condition for BranchCondOp for simplicity or if
    // allowed.
    // Wait, BranchCondOp takes a condition. Since tests are strict about types, let's assume
    // boolean or make sure.
    // IntegerT is not BooleanT. Let's check BranchCondOp requirements or just use BranchOp for
    // simple non-conditional if possible? No, simply branching to two blocks needs Cond.
    // Let's assume ConstantOp(1) acts as true/false or use a proper check.
    // BranchCondOp constructor: BranchCondOp(Value condition, Block target, Block elseTarget)

    entryBlock.addOperation(new BranchCondOp(cond.getValue(), leftBlock, rightBlock));

    // Left block: defines val, branches to merge
    var val = leftBlock.addOperation(new ConstantOp(100));
    leftBlock.addOperation(new BranchOp(mergeBlock));

    // Right block: branches to merge (does NOT define val)
    rightBlock.addOperation(new BranchOp(mergeBlock));

    // Merge block: uses val
    // This is a violation because 'val' is not defined on the path through 'rightBlock'.
    mergeBlock.addOperation(new PrintOp(val.getValue()));
    mergeBlock.addOperation(new ReturnOp());

    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void reachingDefDiamondShape() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcOp = entry.getRight();
    Block entryBlock = funcOp.getEntryBlock();

    Block leftBlock = funcOp.addBlock(new Block());
    Block rightBlock = funcOp.addBlock(new Block());
    Block mergeBlock = funcOp.addBlock(new Block());

    // Entry defines val
    var val = entryBlock.addOperation(new ConstantOp(true));
    entryBlock.addOperation(new BranchCondOp(val.getValue(), leftBlock, rightBlock));

    // Left uses val
    leftBlock.addOperation(new PrintOp(val.getValue()));
    leftBlock.addOperation(new BranchOp(mergeBlock));

    // Right uses val
    rightBlock.addOperation(new PrintOp(val.getValue()));
    rightBlock.addOperation(new BranchOp(mergeBlock));

    // Merge uses val
    mergeBlock.addOperation(new PrintOp(val.getValue()));
    mergeBlock.addOperation(new ReturnOp());

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }
}

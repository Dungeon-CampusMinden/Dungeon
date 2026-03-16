import dgir.core.Dialect;
import dgir.core.debug.Location;
import dgir.core.ir.Block;
import dgir.core.serialization.Utils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.cf.CfOps.BranchCondOp;
import static dgir.dialect.cf.CfOps.BranchOp;
import static dgir.dialect.func.FuncOps.FuncOp;
import static dgir.dialect.func.FuncOps.ReturnOp;
import static dgir.dialect.io.IoOps.PrintOp;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * These are test for checking the validity of the core IR and traits. These test are mainly there
 * to check if the structural analysis of the IR hold, especially reaching definitions and in that
 * context region visiblity, nesting and isolation.
 */
public class CoreTests {
  static final Location LOC = Location.UNKNOWN;
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

    var constOp = funcOp.addOperation(new ConstantOp(LOC, 42), 0);
    funcOp.addOperation(new PrintOp(LOC, constOp.getResult()), 0);
    funcOp.addOperation(new ReturnOp(LOC), 0);

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
    var constOp = entryBlock.addOperation(new ConstantOp(LOC, 42));
    entryBlock.addOperation(new BranchOp(LOC, targetBlock));

    // Target block: use val, return
    targetBlock.addOperation(new PrintOp(LOC, constOp.getResult()));
    targetBlock.addOperation(new ReturnOp(LOC));

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
    var cond = entryBlock.addOperation(new ConstantOp(LOC, true));
    entryBlock.addOperation(new BranchCondOp(LOC, cond.getResult(), leftBlock, rightBlock));

    // Left block: defines val, branches to merge
    var val = leftBlock.addOperation(new ConstantOp(LOC, 100));
    leftBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // Right block: branches to merge (does NOT define val)
    rightBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // Merge block: uses val
    // This is a violation because 'val' is not defined on the path through 'rightBlock'.
    mergeBlock.addOperation(new PrintOp(LOC, val.getResult()));
    mergeBlock.addOperation(new ReturnOp(LOC));

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
    var val = entryBlock.addOperation(new ConstantOp(LOC, true));
    entryBlock.addOperation(new BranchCondOp(LOC, val.getResult(), leftBlock, rightBlock));

    // Left uses val
    leftBlock.addOperation(new PrintOp(LOC, val.getResult()));
    leftBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // Right uses val
    rightBlock.addOperation(new PrintOp(LOC, val.getResult()));
    rightBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // Merge uses val
    mergeBlock.addOperation(new PrintOp(LOC, val.getResult()));
    mergeBlock.addOperation(new ReturnOp(LOC));

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }
}

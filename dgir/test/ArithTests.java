import core.Dialect;
import core.debug.Location;
import dialect.builtin.ProgramOp;
import dialect.builtin.attributes.IntegerAttribute;
import dialect.builtin.types.FloatT;
import dialect.builtin.types.IntegerT;
import dialect.builtin.types.StringT;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static dialect.arith.ArithAttr.*;
import static org.junit.jupiter.api.Assertions.*;
import static dialect.arith.ArithOps.*;

public class ArithTests {
  private static final Location LOC = Location.UNKNOWN;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
  }

  @Test
  public void integerArithmeticOps() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 40), 0);
    var int64Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(2, IntegerT.INT64)), 0);

    var addOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int32Op.getValue(), int64Op.getValue(), BinModeAttr.Mode.ADD), 0);
    var subOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int64Op.getValue(), int32Op.getValue(), BinModeAttr.Mode.SUB), 0);
    var mulOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int32Op.getValue(), int64Op.getValue(), BinModeAttr.Mode.MUL), 0);
    var divOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int64Op.getValue(), int32Op.getValue(), BinModeAttr.Mode.DIV), 0);
    var remOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int64Op.getValue(), int32Op.getValue(), BinModeAttr.Mode.MOD), 0);

    assertEquals(IntegerT.INT64, addOp.getOutputValue().orElseThrow().getType());
    assertEquals(IntegerT.INT64, subOp.getOutputValue().orElseThrow().getType());
    assertEquals(IntegerT.INT64, mulOp.getOutputValue().orElseThrow().getType());
    assertEquals(IntegerT.INT64, divOp.getOutputValue().orElseThrow().getType());
    assertEquals(IntegerT.INT64, remOp.getOutputValue().orElseThrow().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void mixedTypeArithmeticDominatesFloat() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 7), 0);
    var float64Op = funcMain.addOperation(new CastOp(LOC, int32Op.getValue(), FloatT.FLOAT64), 0);

    var addOp =
        funcMain.addOperation(
            new BinaryOp(
                LOC,
                int32Op.getValue(),
                float64Op.getOutputValue().orElseThrow(),
                BinModeAttr.Mode.ADD),
            0);

    assertEquals(FloatT.FLOAT64, addOp.getOutputValue().orElseThrow().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void mixedTypeCompareReturnsBool() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 3), 0);
    var float64Op = funcMain.addOperation(new CastOp(LOC, int32Op.getValue(), FloatT.FLOAT64), 0);

    var cmpOp =
        funcMain.addOperation(
            new CompareOp(
                LOC,
                int32Op.getValue(),
                float64Op.getOutputValue().orElseThrow(),
                CompModeAttr.Mode.LT),
            0);

    assertEquals(IntegerT.BOOL, cmpOp.getOutputValue().orElseThrow().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void castImplicitConversions() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 123), 0);
    var float64Op = funcMain.addOperation(new CastOp(LOC, int32Op.getValue(), FloatT.FLOAT64), 0);
    var int16Op =
        funcMain.addOperation(
            new CastOp(LOC, float64Op.getOutputValue().orElseThrow(), IntegerT.INT16), 0);

    assertEquals(FloatT.FLOAT64, float64Op.getOutputValue().orElseThrow().getType());
    assertEquals(IntegerT.INT16, int16Op.getOutputValue().orElseThrow().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void castRejectsNonNumericTarget() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 1), 0);
    funcMain.addOperation(new CastOp(LOC, int32Op.getValue(), StringT.INSTANCE), 0);

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void compareRejectsNonNumericOperands() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var stringOp = funcMain.addOperation(new ConstantOp(LOC, "nope"), 0);
    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 1), 0);

    funcMain.addOperation(
        new CompareOp(
            LOC,
            stringOp.getValue(),
            int32Op.getValue(),
            CompModeAttr.Mode.EQ),
        0);

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void addRejectsNonNumericOperands() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    FuncOp funcMain = entry.getRight();

    var stringOp = funcMain.addOperation(new ConstantOp(LOC, "oops"), 0);
    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 1), 0);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            funcMain.addOperation(
                new BinaryOp(LOC, stringOp.getValue(), int32Op.getValue(), BinModeAttr.Mode.ADD),
                0));
  }

  @Test
  public void castRejectsNonNumericOperand() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var stringOp = funcMain.addOperation(new ConstantOp(LOC, "nope"), 0);
    funcMain.addOperation(new CastOp(LOC, stringOp.getValue(), IntegerT.INT32), 0);

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }
}

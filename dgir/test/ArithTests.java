import dgir.core.Dialect;
import dgir.core.debug.Location;
import dgir.dialect.str.StrTypes;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static dgir.dialect.arith.ArithAttrs.BinModeAttr;
import static dgir.dialect.arith.ArithOps.*;
import static dgir.dialect.builtin.BuiltinAttrs.FloatAttribute;
import static dgir.dialect.builtin.BuiltinAttrs.IntegerAttribute;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.builtin.BuiltinTypes.*;
import static dgir.dialect.func.FuncOps.FuncOp;
import static dgir.dialect.func.FuncOps.ReturnOp;
import static org.junit.jupiter.api.Assertions.*;

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
            new BinaryOp(LOC, int32Op.getValue(), int64Op.getValue(), BinModeAttr.BinMode.ADD), 0);
    var subOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int64Op.getValue(), int32Op.getValue(), BinModeAttr.BinMode.SUB), 0);
    var mulOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int32Op.getValue(), int64Op.getValue(), BinModeAttr.BinMode.MUL), 0);
    var divOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int64Op.getValue(), int32Op.getValue(), BinModeAttr.BinMode.DIV), 0);
    var remOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int64Op.getValue(), int32Op.getValue(), BinModeAttr.BinMode.MOD), 0);

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
                BinModeAttr.BinMode.ADD),
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
            new BinaryOp(
                LOC,
                int32Op.getValue(),
                float64Op.getOutputValue().orElseThrow(),
                BinModeAttr.BinMode.LT),
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
    funcMain.addOperation(new CastOp(LOC, int32Op.getValue(), StrTypes.StringT.INSTANCE), 0);

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
        new BinaryOp(LOC, stringOp.getValue(), int32Op.getValue(), BinModeAttr.BinMode.EQ), 0);

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
                new BinaryOp(LOC, stringOp.getValue(), int32Op.getValue(), BinModeAttr.BinMode.ADD),
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

  @Test
  public void mixedSignedUnsignedArithmeticDominantType() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int16Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(-7, IntegerT.INT16)), 0);
    var uint32Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(11, IntegerT.UINT32)), 0);

    var addOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int16Op.getValue(), uint32Op.getValue(), BinModeAttr.BinMode.ADD), 0);
    var mulOp =
        funcMain.addOperation(
            new BinaryOp(LOC, uint32Op.getValue(), int16Op.getValue(), BinModeAttr.BinMode.MUL), 0);

    assertEquals(IntegerT.UINT32, addOp.getOutputValue().orElseThrow().getType());
    assertEquals(IntegerT.UINT32, mulOp.getOutputValue().orElseThrow().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void mixedSignedUnsignedFloatArithmeticDominantType() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int16Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(5, IntegerT.INT16)), 0);
    var uint64Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(9, IntegerT.UINT64)), 0);
    var float32Op =
        funcMain.addOperation(new ConstantOp(LOC, new FloatAttribute(1.5f, FloatT.FLOAT32)), 0);

    var add32 =
        funcMain.addOperation(
            new BinaryOp(LOC, int16Op.getValue(), float32Op.getValue(), BinModeAttr.BinMode.ADD),
            0);
    var add64 =
        funcMain.addOperation(
            new BinaryOp(LOC, uint64Op.getValue(), float32Op.getValue(), BinModeAttr.BinMode.ADD),
            0);

    assertEquals(FloatT.FLOAT32, add32.getOutputValue().orElseThrow().getType());
    assertEquals(FloatT.FLOAT64, add64.getOutputValue().orElseThrow().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void mixedSignedUnsignedFloatComparisonsReturnBool() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(-3, IntegerT.INT32)), 0);
    var uint16Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(255, IntegerT.UINT8)), 0);
    var float64Op =
        funcMain.addOperation(new ConstantOp(LOC, new FloatAttribute(3.0, FloatT.FLOAT64)), 0);

    var eqOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int32Op.getValue(), uint16Op.getValue(), BinModeAttr.BinMode.EQ), 0);
    var ltOp =
        funcMain.addOperation(
            new BinaryOp(LOC, uint16Op.getValue(), float64Op.getValue(), BinModeAttr.BinMode.LT),
            0);

    assertEquals(IntegerT.BOOL, eqOp.getOutputValue().orElseThrow().getType());
    assertEquals(IntegerT.BOOL, ltOp.getOutputValue().orElseThrow().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void unsignedOutputTest() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(255, IntegerT.INT8)), 0);
    var uint16Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(255, IntegerT.UINT8)), 0);
    var float64Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(-1, IntegerT.UINT16)), 0);
    var returnOp = funcMain.addOperation(new ReturnOp(LOC), 0);

    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void unsignedOnlyModesRejectFloatingOperands() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var uint32Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(6, IntegerT.UINT32)), 0);
    var float32Op =
        funcMain.addOperation(new ConstantOp(LOC, new FloatAttribute(2.0f, FloatT.FLOAT32)), 0);

    funcMain.addOperation(
        new BinaryOp(LOC, uint32Op.getValue(), float32Op.getValue(), BinModeAttr.BinMode.DIVUI), 0);
    funcMain.addOperation(
        new BinaryOp(LOC, uint32Op.getValue(), float32Op.getValue(), BinModeAttr.BinMode.MODUI), 0);

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void unsignedOnlyModesAcceptMixedSignedAndUnsignedIntegers() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(-8, IntegerT.INT32)), 0);
    var uint16Op =
        funcMain.addOperation(new ConstantOp(LOC, new IntegerAttribute(4, IntegerT.UINT16)), 0);

    var divuiOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int32Op.getValue(), uint16Op.getValue(), BinModeAttr.BinMode.DIVUI),
            0);
    var moduiOp =
        funcMain.addOperation(
            new BinaryOp(LOC, int32Op.getValue(), uint16Op.getValue(), BinModeAttr.BinMode.MODUI),
            0);

    assertEquals(IntegerT.INT32, divuiOp.getOutputValue().orElseThrow().getType());
    assertEquals(IntegerT.INT32, moduiOp.getOutputValue().orElseThrow().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }
}

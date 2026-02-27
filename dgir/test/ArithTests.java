import core.Dialect;
import core.ir.Location;
import dialect.arith.AddOp;
import dialect.arith.CastOp;
import dialect.arith.CompareOp;
import dialect.arith.ConstantOp;
import dialect.arith.DivOp;
import dialect.arith.MulOp;
import dialect.arith.RemOp;
import dialect.arith.SubOp;
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

    var addOp = funcMain.addOperation(new AddOp(LOC, int32Op.getResult(), int64Op.getResult()), 0);
    var subOp = funcMain.addOperation(new SubOp(LOC, int64Op.getResult(), int32Op.getResult()), 0);
    var mulOp = funcMain.addOperation(new MulOp(LOC, int32Op.getResult(), int64Op.getResult()), 0);
    var divOp = funcMain.addOperation(new DivOp(LOC, int64Op.getResult(), int32Op.getResult()), 0);
    var remOp = funcMain.addOperation(new RemOp(LOC, int64Op.getResult(), int32Op.getResult()), 0);

    assertEquals(IntegerT.INT64, addOp.getResult().getType());
    assertEquals(IntegerT.INT64, subOp.getResult().getType());
    assertEquals(IntegerT.INT64, mulOp.getResult().getType());
    assertEquals(IntegerT.INT64, divOp.getResult().getType());
    assertEquals(IntegerT.INT64, remOp.getResult().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void mixedTypeArithmeticDominatesFloat() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 7), 0);
    var float64Op = funcMain.addOperation(new CastOp(LOC, int32Op.getResult(), FloatT.FLOAT64), 0);

    var addOp =
        funcMain.addOperation(new AddOp(LOC, int32Op.getResult(), float64Op.getResult()), 0);

    assertEquals(FloatT.FLOAT64, addOp.getResult().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void mixedTypeCompareReturnsBool() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 3), 0);
    var float64Op = funcMain.addOperation(new CastOp(LOC, int32Op.getResult(), FloatT.FLOAT64), 0);

    var cmpOp =
        funcMain.addOperation(
            new CompareOp(
                LOC,
                int32Op.getResult(),
                float64Op.getResult(),
                dialect.arith.attributes.CompModeAttr.CompMode.LT),
            0);

    assertEquals(IntegerT.BOOL, cmpOp.getResult().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void castImplicitConversions() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 123), 0);
    var float64Op = funcMain.addOperation(new CastOp(LOC, int32Op.getResult(), FloatT.FLOAT64), 0);
    var int16Op = funcMain.addOperation(new CastOp(LOC, float64Op.getResult(), IntegerT.INT16), 0);

    assertEquals(FloatT.FLOAT64, float64Op.getResult().getType());
    assertEquals(IntegerT.INT16, int16Op.getResult().getType());

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertTrue(TestUtils.testValidityAndSerialization(programOp));
  }

  @Test
  public void castRejectsNonNumericTarget() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var int32Op = funcMain.addOperation(new ConstantOp(LOC, 1), 0);
    funcMain.addOperation(new CastOp(LOC, int32Op.getResult(), StringT.INSTANCE), 0);

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
            stringOp.getResult(),
            int32Op.getResult(),
            dialect.arith.attributes.CompModeAttr.CompMode.EQ),
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
        () -> funcMain.addOperation(new AddOp(LOC, stringOp.getResult(), int32Op.getResult()), 0));
  }

  @Test
  public void castRejectsNonNumericOperand() {
    Pair<ProgramOp, FuncOp> entry = TestUtils.createProgramOpWithEntryFunc();
    ProgramOp programOp = entry.getLeft();
    FuncOp funcMain = entry.getRight();

    var stringOp = funcMain.addOperation(new ConstantOp(LOC, "nope"), 0);
    funcMain.addOperation(new CastOp(LOC, stringOp.getResult(), IntegerT.INT32), 0);

    funcMain.addOperation(new ReturnOp(LOC), 0);
    assertFalse(TestUtils.testValidityAndSerialization(programOp));
  }
}

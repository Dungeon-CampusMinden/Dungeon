import dgir.core.debug.Location;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dgir.dialect.arith.ArithAttrs.BinModeAttr;
import static dgir.dialect.arith.ArithOps.BinaryOp;
import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.builtin.BuiltinAttrs.FloatAttribute;
import static dgir.dialect.builtin.BuiltinAttrs.IntegerAttribute;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.builtin.BuiltinTypes.FloatT;
import static dgir.dialect.builtin.BuiltinTypes.IntegerT;
import static dgir.dialect.func.FuncOps.FuncOp;
import static dgir.dialect.func.FuncOps.ReturnOp;
import static dgir.dialect.io.IoOps.PrintOp;

/**
 * VM-level execution tests for arith runner semantics across signed, unsigned, and float inputs.
 */
public class ArithVmTests extends VmTestBase {
  private static final Location LOC = Location.UNKNOWN;

  @Test
  void floatComparisonUsesFloatingPointSemantics() {
    ProgramOp program = new ProgramOp(LOC);
    FuncOp main = program.addOperation(new FuncOp(LOC, "main"));

    var lhs = main.addOperation(new ConstantOp(LOC, new FloatAttribute(1.9f, FloatT.FLOAT32)), 0);
    var rhs = main.addOperation(new ConstantOp(LOC, new FloatAttribute(1.2f, FloatT.FLOAT32)), 0);
    var gt =
        main.addOperation(
            new BinaryOp(LOC, lhs.getResult(), rhs.getResult(), BinModeAttr.BinMode.GT), 0);

    main.addOperation(new PrintOp(LOC, gt.getResult()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(program, "true");
  }

  @Test
  void unsignedDivisionAndModuloUseUnsignedSemantics() {
    ProgramOp program = new ProgramOp(LOC);
    FuncOp main = program.addOperation(new FuncOp(LOC, "main"));

    var lhs = main.addOperation(new ConstantOp(LOC, new IntegerAttribute(-1, IntegerT.UINT32)), 0);
    var rhs = main.addOperation(new ConstantOp(LOC, new IntegerAttribute(2, IntegerT.UINT32)), 0);
    var div =
        main.addOperation(
            new BinaryOp(LOC, lhs.getResult(), rhs.getResult(), BinModeAttr.BinMode.DIVUI), 0);
    var mod =
        main.addOperation(
            new BinaryOp(LOC, lhs.getResult(), rhs.getResult(), BinModeAttr.BinMode.MODUI), 0);

    var format = main.addOperation(new ConstantOp(LOC, "%d,%d\n"), 0);
    main.addOperation(
        new PrintOp(LOC, List.of(format.getResult(), div.getResult(), mod.getResult())), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(program, "2147483647,1\n");
  }

  @Test
  void mixedSignedUnsignedComparisonUsesUnsignedDominance() {
    ProgramOp program = new ProgramOp(LOC);
    FuncOp main = program.addOperation(new FuncOp(LOC, "main"));

    var lhs = main.addOperation(new ConstantOp(LOC, new IntegerAttribute(-1, IntegerT.INT32)), 0);
    var rhs = main.addOperation(new ConstantOp(LOC, new IntegerAttribute(1, IntegerT.UINT32)), 0);
    var lt =
        main.addOperation(
            new BinaryOp(LOC, lhs.getResult(), rhs.getResult(), BinModeAttr.BinMode.LT), 0);

    main.addOperation(new PrintOp(LOC, lt.getResult()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(program, "false");
  }

  @Test
  void unsignedRightShiftRespectsOperandBitWidth() {
    ProgramOp program = new ProgramOp(LOC);
    FuncOp main = program.addOperation(new FuncOp(LOC, "main"));

    var lhs = main.addOperation(new ConstantOp(LOC, new IntegerAttribute(255, IntegerT.UINT8)), 0);
    var rhs = main.addOperation(new ConstantOp(LOC, 1), 0);
    var shifted =
        main.addOperation(
            new BinaryOp(LOC, lhs.getResult(), rhs.getResult(), BinModeAttr.BinMode.RSHU), 0);

    main.addOperation(new PrintOp(LOC, shifted.getResult()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(program, "127");
  }
}

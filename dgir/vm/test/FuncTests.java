import dgir.core.debug.Location;
import dgir.dialect.func.FuncOps;
import org.junit.jupiter.api.Test;

import java.util.List;

import static dgir.dialect.arith.ArithAttrs.BinModeAttr;
import static dgir.dialect.arith.ArithOps.BinaryOp;
import static dgir.dialect.arith.ArithOps.ConstantOp;
import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.builtin.BuiltinTypes.IntegerT;
import static dgir.dialect.func.FuncOps.CallIndirectOp;
import static dgir.dialect.func.FuncOps.FuncOp;
import static dgir.dialect.func.FuncOps.ReturnOp;
import static dgir.dialect.func.FuncTypes.FuncType;
import static dgir.dialect.io.IoOps.PrintOp;

/**
 * VM-level execution tests for the {@code func} dialect.
 *
 * <p>Tests cover {@code func.constant} (function-reference materialisation) and {@code
 * func.call_indirect} (indirect dispatch through a first-class function reference).
 */
public class FuncTests extends VmTestBase {
  static final Location LOC = Location.UNKNOWN;

  // =========================================================================
  // func.call_indirect — no arguments
  // =========================================================================

  /**
   * A {@code func.constant} reference to a no-arg callee can be used with {@code
   * func.call_indirect}; the callee's return value is propagated back to the call site.
   */
  @Test
  void callIndirect_noArgFunction_returnsValue() {
    ProgramOp program = new ProgramOp(LOC);

    // target: () -> int32  { return 42; }
    FuncOp target =
        program.addOperation(new FuncOp(LOC, "target", FuncType.of(List.of(), IntegerT.INT32)));
    var c42 = target.addOperation(new ConstantOp(LOC, 42), 0);
    target.addOperation(new ReturnOp(LOC, c42.getResult()), 0);

    // main: ref = constant target; result = call_indirect ref(); print result
    FuncOp main = program.addOperation(new FuncOp(LOC, "main"));
    var funcRef = main.addOperation(new FuncOps.ConstantOp(LOC, target), 0);
    var callResult = main.addOperation(new CallIndirectOp(LOC, funcRef.getResult(), List.of()), 0);
    main.addOperation(new PrintOp(LOC, callResult.getOutputValue().orElseThrow()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(program, "42");
  }

  /**
   * An indirect call to a void (no-return) function executes the callee body; side-effects (here a
   * print) are observable even though the call site has no result value.
   */
  @Test
  void callIndirect_voidFunction_executesBody() {
    ProgramOp program = new ProgramOp(LOC);

    // greet: () -> void  { print "hello\n"; }
    FuncOp greetFunc = program.addOperation(new FuncOp(LOC, "greet", FuncType.empty()));
    var msg = greetFunc.addOperation(new ConstantOp(LOC, "hello\n"), 0);
    greetFunc.addOperation(new PrintOp(LOC, msg.getResult()), 0);
    greetFunc.addOperation(new ReturnOp(LOC), 0);

    // main: ref = constant greet; call_indirect ref()
    FuncOp main = program.addOperation(new FuncOp(LOC, "main"));
    var funcRef = main.addOperation(new FuncOps.ConstantOp(LOC, greetFunc), 0);
    main.addOperation(new CallIndirectOp(LOC, funcRef.getResult(), List.of()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(program, "hello\n");
  }

  // =========================================================================
  // func.call_indirect — argument passing
  // =========================================================================

  /**
   * A single argument is forwarded correctly through {@code func.call_indirect}: doubling 5 must
   * yield 10.
   */
  @Test
  void callIndirect_singleArg_passesArgumentCorrectly() {
    ProgramOp program = new ProgramOp(LOC);

    // doubleIt: (x: int32) -> int32  { return x + x; }
    FuncType doubleType = FuncType.of(List.of(IntegerT.INT32), IntegerT.INT32);
    FuncOp doubleFunc = program.addOperation(new FuncOp(LOC, "doubleIt", doubleType));
    var x = doubleFunc.getArgument(0).orElseThrow();
    var xPlusX = doubleFunc.addOperation(new BinaryOp(LOC, x, x, BinModeAttr.BinMode.ADD), 0);
    doubleFunc.addOperation(new ReturnOp(LOC, xPlusX.getResult()), 0);

    // main: ref = constant doubleIt; result = call_indirect ref(5); print result
    FuncOp main = program.addOperation(new FuncOp(LOC, "main"));
    var funcRef = main.addOperation(new FuncOps.ConstantOp(LOC, doubleFunc), 0);
    var arg = main.addOperation(new ConstantOp(LOC, 5), 0);
    var callResult =
        main.addOperation(
            new CallIndirectOp(LOC, funcRef.getResult(), List.of(arg.getResult())), 0);
    main.addOperation(new PrintOp(LOC, callResult.getOutputValue().orElseThrow()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(program, "10");
  }

  /**
   * Multiple arguments are all forwarded in the correct order: {@code add(3, 4)} via an indirect
   * reference must produce 7.
   */
  @Test
  void callIndirect_multipleArgs_allForwardedCorrectly() {
    ProgramOp program = new ProgramOp(LOC);

    // add: (a: int32, b: int32) -> int32  { return a + b; }
    FuncType addType = FuncType.of(List.of(IntegerT.INT32, IntegerT.INT32), IntegerT.INT32);
    FuncOp addFunc = program.addOperation(new FuncOp(LOC, "add", addType));
    var a = addFunc.getArgument(0).orElseThrow();
    var b = addFunc.getArgument(1).orElseThrow();
    var sum = addFunc.addOperation(new BinaryOp(LOC, a, b, BinModeAttr.BinMode.ADD), 0);
    addFunc.addOperation(new ReturnOp(LOC, sum.getResult()), 0);

    // main: ref = constant add; result = call_indirect ref(3, 4); print result
    FuncOp main = program.addOperation(new FuncOp(LOC, "main"));
    var funcRef = main.addOperation(new FuncOps.ConstantOp(LOC, addFunc), 0);
    var c3 = main.addOperation(new ConstantOp(LOC, 3), 0);
    var c4 = main.addOperation(new ConstantOp(LOC, 4), 0);
    var callResult =
        main.addOperation(
            new CallIndirectOp(LOC, funcRef.getResult(), List.of(c3.getResult(), c4.getResult())),
            0);
    main.addOperation(new PrintOp(LOC, callResult.getOutputValue().orElseThrow()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(program, "7");
  }

  // =========================================================================
  // func.call_indirect — dispatch behaviour
  // =========================================================================

  /**
   * Two independent function references each dispatch to the correct callee: one performs addition,
   * the other multiplication.
   */
  @Test
  void callIndirect_twoReferences_dispatchToCorrectCallee() {
    ProgramOp program = new ProgramOp(LOC);

    FuncType binType = FuncType.of(List.of(IntegerT.INT32, IntegerT.INT32), IntegerT.INT32);

    // add: (a, b) -> a + b
    FuncOp addFunc = program.addOperation(new FuncOp(LOC, "add", binType));
    {
      var aa = addFunc.getArgument(0).orElseThrow();
      var bb = addFunc.getArgument(1).orElseThrow();
      addFunc.addOperation(
          new ReturnOp(
              LOC,
              addFunc
                  .addOperation(new BinaryOp(LOC, aa, bb, BinModeAttr.BinMode.ADD), 0)
                  .getResult()),
          0);
    }

    // mul: (a, b) -> a * b
    FuncOp mulFunc = program.addOperation(new FuncOp(LOC, "mul", binType));
    {
      var aa = mulFunc.getArgument(0).orElseThrow();
      var bb = mulFunc.getArgument(1).orElseThrow();
      mulFunc.addOperation(
          new ReturnOp(
              LOC,
              mulFunc
                  .addOperation(new BinaryOp(LOC, aa, bb, BinModeAttr.BinMode.MUL), 0)
                  .getResult()),
          0);
    }

    // main: call add(3,4) then mul(3,4) via separate indirect refs; print "%d\n" for each
    FuncOp main = program.addOperation(new FuncOp(LOC, "main"));
    var c3 = main.addOperation(new ConstantOp(LOC, 3), 0);
    var c4 = main.addOperation(new ConstantOp(LOC, 4), 0);
    var fmt = main.addOperation(new ConstantOp(LOC, "%d\n"), 0);

    var addRef = main.addOperation(new FuncOps.ConstantOp(LOC, addFunc), 0);
    var addResult =
        main.addOperation(
            new CallIndirectOp(LOC, addRef.getResult(), List.of(c3.getResult(), c4.getResult())),
            0);
    main.addOperation(
        new PrintOp(LOC, fmt.getResult(), addResult.getOutputValue().orElseThrow()), 0);

    var mulRef = main.addOperation(new FuncOps.ConstantOp(LOC, mulFunc), 0);
    var mulResult =
        main.addOperation(
            new CallIndirectOp(LOC, mulRef.getResult(), List.of(c3.getResult(), c4.getResult())),
            0);
    main.addOperation(
        new PrintOp(LOC, fmt.getResult(), mulResult.getOutputValue().orElseThrow()), 0);

    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(program, "7\n12\n");
  }

  /**
   * The same function reference can be used for multiple successive indirect calls; each invocation
   * receives its own independent argument (double negation must restore the original).
   */
  @Test
  void callIndirect_sameReferenceCalledTwice_independentResults() {
    ProgramOp program = new ProgramOp(LOC);

    // negate: (x: int32) -> int32  { return 0 - x; }
    FuncType negType = FuncType.of(List.of(IntegerT.INT32), IntegerT.INT32);
    FuncOp negFunc = program.addOperation(new FuncOp(LOC, "negate", negType));
    {
      var param = negFunc.getArgument(0).orElseThrow();
      var zero = negFunc.addOperation(new ConstantOp(LOC, 0), 0);
      var neg =
          negFunc.addOperation(
              new BinaryOp(LOC, zero.getResult(), param, BinModeAttr.BinMode.SUB), 0);
      negFunc.addOperation(new ReturnOp(LOC, neg.getResult()), 0);
    }

    // main: ref = constant negate
    //       r1  = call_indirect ref(3)    // → -3
    //       r2  = call_indirect ref(r1)   // → 3  (double negation)
    //       print r2
    FuncOp main = program.addOperation(new FuncOp(LOC, "main"));
    var funcRef = main.addOperation(new FuncOps.ConstantOp(LOC, negFunc), 0);
    var c3 = main.addOperation(new ConstantOp(LOC, 3), 0);
    var r1 =
        main.addOperation(new CallIndirectOp(LOC, funcRef.getResult(), List.of(c3.getResult())), 0);
    var r2 =
        main.addOperation(
            new CallIndirectOp(
                LOC, funcRef.getResult(), List.of(r1.getOutputValue().orElseThrow())),
            0);
    main.addOperation(new PrintOp(LOC, r2.getOutputValue().orElseThrow()), 0);
    main.addOperation(new ReturnOp(LOC), 0);

    runProgram(program, "3");
  }
}

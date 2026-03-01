import core.debug.Location;
import core.ir.Block;
import dgir.vm.api.VM;
import dgir.vm.dialect.io.ConsoleInRunner;
import dialect.func.CallOp;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.func.types.FuncType;
import dialect.io.ConsoleInOp;
import dialect.io.PrintOp;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static dialect.arith.ArithAttrs.*;
import static dialect.arith.ArithOps.*;
import static dialect.builtin.BuiltinOps.ProgramOp;
import static dialect.builtin.BuiltinTypes.*;
import static dialect.cf.CfOps.*;
import static java.nio.charset.StandardCharsets.UTF_8;

/** Testcases for the VM, only testing output from and to the console. */
public class VmConsoleTest extends VmTestBase {
  static final Location LOC = Location.UNKNOWN;

  /**
   * Creates a simple dgir program printing "Hello World!" to the console and runs it through the
   * VM.
   */
  @Test
  void helloWorldTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp funcOp = programOp.addOperation(new FuncOp(LOC, "main"));
    var text = funcOp.addOperation(new ConstantOp(LOC, "Hello World!\n"), 0);
    funcOp.addOperation(new PrintOp(LOC, List.of(text.getValue())), 0);
    funcOp.addOperation(new ReturnOp(LOC), 0);

    runProgram(programOp, "Hello World!\n");
  }

  /** Same as helloWorldTest but the string is produced by a function call. */
  @Test
  void helloWorldCallTest() {
    ProgramOp programOp = new ProgramOp(LOC);

    FuncOp stringOp =
        programOp.addOperation(
            new FuncOp(LOC, "string", new FuncType(List.of(), StringT.INSTANCE)));
    {
      var text = stringOp.addOperation(new ConstantOp(LOC, "Hello World!\n"), 0);
      stringOp.addOperation(new ReturnOp(LOC, text.getValue()), 0);
    }

    {
      FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));
      var text = mainOp.addOperation(new CallOp(LOC, stringOp), 0);
      mainOp.addOperation(new PrintOp(LOC, List.of(text.getOutputValue().orElseThrow())), 0);
      mainOp.addOperation(new ReturnOp(LOC), 0);
    }

    runProgram(programOp, "Hello World!\n");
  }

  /**
   * Tests BranchOp: entry block prints "before", branches unconditionally to a second block that
   * prints "after".
   */
  @Test
  void unconditionalBranchTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block afterBlock = mainOp.addBlock(new Block());

    // Entry block: print "before\n", then branch unconditionally
    var before = entryBlock.addOperation(new ConstantOp(LOC, "before\n"));
    entryBlock.addOperation(new PrintOp(LOC, before.getValue()));
    entryBlock.addOperation(new BranchOp(LOC, afterBlock));

    // After block: print "after\n", then return
    var after = afterBlock.addOperation(new ConstantOp(LOC, "after\n"));
    afterBlock.addOperation(new PrintOp(LOC, after.getValue()));
    afterBlock.addOperation(new ReturnOp(LOC));

    runProgram(programOp, "before\nafter\n");
  }

  /**
   * Tests BranchCondOp taking the true branch: condition is true, so the "true" block is executed.
   */
  @Test
  void conditionalBranchTrueBranchTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block trueBlock = mainOp.addBlock(new Block());
    Block falseBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: define condition = true, branch conditionally
    var cond = entryBlock.addOperation(new ConstantOp(LOC, true));
    entryBlock.addOperation(new BranchCondOp(LOC, cond.getValue(), trueBlock, falseBlock));

    // True block: print "yes\n", jump to merge
    var yes = trueBlock.addOperation(new ConstantOp(LOC, "yes\n"));
    trueBlock.addOperation(new PrintOp(LOC, yes.getValue()));
    trueBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // False block: print "no\n", jump to merge
    var no = falseBlock.addOperation(new ConstantOp(LOC, "no\n"));
    falseBlock.addOperation(new PrintOp(LOC, no.getValue()));
    falseBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // Merge block: return
    mergeBlock.addOperation(new ReturnOp(LOC));

    runProgram(programOp, "yes\n");
  }

  /**
   * Tests BranchCondOp taking the false branch: condition is false, so the "false" block is
   * executed.
   */
  @Test
  void conditionalBranchFalseBranchTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block trueBlock = mainOp.addBlock(new Block());
    Block falseBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: define condition = false, branch conditionally
    var cond = entryBlock.addOperation(new ConstantOp(LOC, false));
    entryBlock.addOperation(new BranchCondOp(LOC, cond.getValue(), trueBlock, falseBlock));

    // True block: print "yes\n", jump to merge
    var yes = trueBlock.addOperation(new ConstantOp(LOC, "yes\n"));
    trueBlock.addOperation(new PrintOp(LOC, yes.getValue()));
    trueBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // False block: print "no\n", jump to merge
    var no = falseBlock.addOperation(new ConstantOp(LOC, "no\n"));
    falseBlock.addOperation(new PrintOp(LOC, no.getValue()));
    falseBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // Merge block: return
    mergeBlock.addOperation(new ReturnOp(LOC));

    runProgram(programOp, "no\n");
  }

  /**
   * Tests BranchCondOp where the condition is provided by a function call. A helper function
   * returns a boolean constant; the main function uses it as a branch condition.
   */
  @Test
  void conditionalBranchFromFunctionCallTest() {
    ProgramOp programOp = new ProgramOp(LOC);

    // Helper function: returns true (int1)
    FuncOp condFunc =
        programOp.addOperation(
            new FuncOp(LOC, "getCondition", new FuncType(List.of(), IntegerT.BOOL)));
    {
      var t = condFunc.addOperation(new ConstantOp(LOC, true), 0);
      condFunc.addOperation(new ReturnOp(LOC, t.getValue()), 0);
    }

    // Main function
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block trueBlock = mainOp.addBlock(new Block());
    Block falseBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: call getCondition(), branch on its result
    var callResult = entryBlock.addOperation(new CallOp(LOC, condFunc));
    entryBlock.addOperation(
        new BranchCondOp(LOC, callResult.getOutputValue().orElseThrow(), trueBlock, falseBlock));

    // True block
    var yes = trueBlock.addOperation(new ConstantOp(LOC, "condition true\n"));
    trueBlock.addOperation(new PrintOp(LOC, yes.getValue()));
    trueBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // False block
    var no = falseBlock.addOperation(new ConstantOp(LOC, "condition false\n"));
    falseBlock.addOperation(new PrintOp(LOC, no.getValue()));
    falseBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // Merge block: return
    mergeBlock.addOperation(new ReturnOp(LOC));

    runProgram(programOp, "condition true\n");
  }

  /**
   * Tests a function call where the callee itself uses BranchCondOp internally to decide what to
   * print.
   */
  @Test
  void functionCallWithInternalBranchTest() {
    ProgramOp programOp = new ProgramOp(LOC);

    // Helper function: takes an int1 parameter and prints "positive\n" or "non-positive\n"
    FuncOp printFunc =
        programOp.addOperation(
            new FuncOp(LOC, "printBranch", new FuncType(List.of(IntegerT.BOOL), null)));
    {
      Block funcEntry = printFunc.getEntryBlock();
      Block posBlock = printFunc.addBlock(new Block());
      Block negBlock = printFunc.addBlock(new Block());
      Block retBlock = printFunc.addBlock(new Block());

      // Branch on the parameter
      var param = printFunc.getArgument(0).orElseThrow();
      funcEntry.addOperation(new BranchCondOp(LOC, param, posBlock, negBlock));

      {
        var posText = posBlock.addOperation(new ConstantOp(LOC, "positive\n"));
        posBlock.addOperation(new PrintOp(LOC, posText.getValue()));
        posBlock.addOperation(new BranchOp(LOC, retBlock));
      }

      {
        var negText = negBlock.addOperation(new ConstantOp(LOC, "non-positive\n"));
        negBlock.addOperation(new PrintOp(LOC, negText.getValue()));
        negBlock.addOperation(new BranchOp(LOC, retBlock));
      }

      retBlock.addOperation(new ReturnOp(LOC));
    }

    // Main: call printBranch(true) then printBranch(false)
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));
    {
      var trueVal = mainOp.addOperation(new ConstantOp(LOC, true), 0);
      var falseVal = mainOp.addOperation(new ConstantOp(LOC, false), 0);
      mainOp.addOperation(new CallOp(LOC, printFunc, trueVal.getValue()), 0);
      mainOp.addOperation(new CallOp(LOC, printFunc, falseVal.getValue()), 0);
      mainOp.addOperation(new ReturnOp(LOC), 0);
    }

    runProgram(programOp, "positive\nnon-positive\n");
  }

  /**
   * Test if console inputs can be used to direct the control flow of the program. The program reads
   * a line from the console, and branches on whether the input is "yes" or "no". The test provides
   * "yes" as input and checks if the correct branch is taken.
   */
  @Test
  void consoleInputBranchTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block yesBlock = mainOp.addBlock(new Block());
    Block noBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: read input from console, compare to "yes", branch conditionally
    var input = entryBlock.addOperation(new ConsoleInOp(LOC, IntegerT.BOOL));
    entryBlock.addOperation(new BranchCondOp(LOC, input.getResult(), yesBlock, noBlock));

    // Yes block: print "You said true!\n", jump to merge
    var yesText = yesBlock.addOperation(new ConstantOp(LOC, "You said true!\n"));
    yesBlock.addOperation(new PrintOp(LOC, yesText.getValue()));
    yesBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // No block: print "You said false!\n", jump to merge
    var noText = noBlock.addOperation(new ConstantOp(LOC, "You said false!\n"));
    noBlock.addOperation(new PrintOp(LOC, noText.getValue()));
    noBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // Merge block: return
    mergeBlock.addOperation(new ReturnOp(LOC));

    VM vm = createVm(programOp);
    // Test positive case
    {
      String simulatedInput = "1\n";
      ConsoleInRunner.setInputStream(new ByteArrayInputStream(simulatedInput.getBytes(UTF_8)));
      runVM(vm, "You said true!\n");
    }
    resetOutput();
    // Test negative case
    {
      String simulatedInput = "0\n";
      ConsoleInRunner.setInputStream(new ByteArrayInputStream(simulatedInput.getBytes(UTF_8)));
      runVM(vm, "You said false!\n");
    }
  }

  /**
   * Tests reading an integer (INT32) from the console and printing it back to the console. The
   * program reads one integer and prints it with a newline.
   */
  @Test
  void consoleInputReadIntTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    // Read an INT32 from the console
    var input = mainOp.addOperation(new ConsoleInOp(LOC, IntegerT.INT32), 0);
    // Print it back
    mainOp.addOperation(new PrintOp(LOC, input.getResult()), 0);
    mainOp.addOperation(new ReturnOp(LOC), 0);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("42\n".getBytes(UTF_8)));
    runProgram(programOp, "42");
  }

  /**
   * Tests reading a string from the console and printing it back to the console. The program reads
   * one line of text and echoes it.
   */
  @Test
  void consoleInputReadStringTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    // Read a String from the console
    var input = mainOp.addOperation(new ConsoleInOp(LOC, StringT.INSTANCE), 0);
    // Print it back
    mainOp.addOperation(new PrintOp(LOC, input.getResult()), 0);
    mainOp.addOperation(new ReturnOp(LOC), 0);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("Hello from stdin\n".getBytes(UTF_8)));
    runProgram(programOp, "Hello from stdin");
  }

  /**
   * Tests formatted printing using a string read from the console. The program reads a name from
   * the console and prints a greeting using printf-style formatting.
   */
  @Test
  void consoleInputFormattedPrintStringTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    // Format string constant
    var fmt = mainOp.addOperation(new ConstantOp(LOC, "Hello, %s!\n"), 0);
    // Read the name from the console
    var name = mainOp.addOperation(new ConsoleInOp(LOC, StringT.INSTANCE), 0);
    // Print formatted: "Hello, <name>!\n"
    mainOp.addOperation(new PrintOp(LOC, fmt.getValue(), name.getResult()), 0);
    mainOp.addOperation(new ReturnOp(LOC), 0);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("World\n".getBytes(UTF_8)));
    runProgram(programOp, "Hello, World!\n");
  }

  /**
   * Tests formatted printing using an integer read from the console. The program reads a number
   * from the console and prints it inside a sentence using printf-style formatting.
   */
  @Test
  void consoleInputFormattedPrintIntTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    // Format string constant
    var fmt = mainOp.addOperation(new ConstantOp(LOC, "The answer is %d.\n"), 0);
    // Read an INT32 from the console
    var number = mainOp.addOperation(new ConsoleInOp(LOC, IntegerT.INT32), 0);
    // Print formatted: "The answer is <number>.\n"
    mainOp.addOperation(new PrintOp(LOC, fmt.getValue(), number.getResult()), 0);
    mainOp.addOperation(new ReturnOp(LOC), 0);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("42\n".getBytes(UTF_8)));
    runProgram(programOp, "The answer is 42.\n");
  }

  /**
   * Tests formatted printing using a float read from the console. The program reads a
   * floating-point number and prints it with two decimal places using printf-style formatting.
   */
  @Test
  void consoleInputFormattedPrintFloatTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    // Format string constant
    var fmt = mainOp.addOperation(new ConstantOp(LOC, "Pi is approximately %.2f.\n"), 0);
    // Read a FLOAT32 from the console
    var number = mainOp.addOperation(new ConsoleInOp(LOC, FloatT.FLOAT32), 0);
    // Print formatted: "Pi is approximately <number>.\n"
    mainOp.addOperation(new PrintOp(LOC, fmt.getValue(), number.getResult()), 0);
    mainOp.addOperation(new ReturnOp(LOC), 0);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("3.14159\n".getBytes(UTF_8)));
    runProgram(programOp, "Pi is approximately 3.14.\n");
  }

  /**
   * Tests a program where a boolean console input drives a branch, and only the true branch reads a
   * second value from the console (an integer) and prints it using a format string. The false
   * branch prints a fixed constant without any further console input.
   *
   * <p>The program structure is:
   *
   * <pre>
   *   entry:  flag = consoleIn(bool)
   *           branchCond(flag, trueBlock, falseBlock)
   *   trueBlock:  n = consoleIn(int32)
   *               print("You entered: %d\n", n)
   *               branch mergeBlock
   *   falseBlock: print("No input given.\n")
   *               branch mergeBlock
   *   mergeBlock: return
   * </pre>
   */
  @Test
  void consoleInputBranchOnlyOneBranchReadsInputTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block trueBlock = mainOp.addBlock(new Block());
    Block falseBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: read a bool from console, branch on it
    var flag = entryBlock.addOperation(new ConsoleInOp(LOC, IntegerT.BOOL));
    entryBlock.addOperation(new BranchCondOp(LOC, flag.getResult(), trueBlock, falseBlock));

    // True block: read an integer from console, print it formatted, jump to merge
    var fmt = trueBlock.addOperation(new ConstantOp(LOC, "You entered: %d\n"));
    var n = trueBlock.addOperation(new ConsoleInOp(LOC, IntegerT.INT32));
    trueBlock.addOperation(new PrintOp(LOC, fmt.getValue(), n.getResult()));
    trueBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // False block: print a fixed message without any console input, jump to merge
    var noInput = falseBlock.addOperation(new ConstantOp(LOC, "No input given.\n"));
    falseBlock.addOperation(new PrintOp(LOC, noInput.getValue()));
    falseBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // Merge block: return
    mergeBlock.addOperation(new ReturnOp(LOC));

    VM vm = createVm(programOp);
    // Test true branch: flag=1 -> read integer 7 -> print "You entered: 7\n"
    {
      ConsoleInRunner.setInputStream(new ByteArrayInputStream("1\n7\n".getBytes(UTF_8)));
      runVM(vm, "You entered: 7\n");
    }
    resetOutput();
    // Test false branch: flag=0 -> no further console read -> print "No input given.\n"
    {
      ConsoleInRunner.setInputStream(new ByteArrayInputStream("0\n".getBytes(UTF_8)));
      runVM(vm, "No input given.\n");
    }
  }

  /**
   * Tests reading multiple values from the console — an integer and a string — and printing them
   * together using printf-style format string with two substitutions.
   */
  @Test
  void consoleInputFormattedPrintMultipleValuesTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    // Format string constant
    var fmt = mainOp.addOperation(new ConstantOp(LOC, "%s scored %d points.\n"), 0);
    // Read a name (string) then a score (int)
    var nameIn = mainOp.addOperation(new ConsoleInOp(LOC, StringT.INSTANCE), 0);
    var scoreIn = mainOp.addOperation(new ConsoleInOp(LOC, IntegerT.INT32), 0);
    // Print formatted
    mainOp.addOperation(
        new PrintOp(LOC, fmt.getValue(), nameIn.getResult(), scoreIn.getResult()), 0);
    mainOp.addOperation(new ReturnOp(LOC), 0);

    // Two consecutive lines: first the name, then the score
    ConsoleInRunner.setInputStream(new ByteArrayInputStream("Alice\n100\n".getBytes(UTF_8)));
    runProgram(programOp, "Alice scored 100 points.\n");
  }

  /** Tests BinaryOp with constant operands: computes 6 * 7 = 42 and prints the result. */
  @Test
  void binaryOpMultiplyConstantsTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    var lhs = mainOp.addOperation(new ConstantOp(LOC, 6), 0);
    var rhs = mainOp.addOperation(new ConstantOp(LOC, 7), 0);
    var product =
        mainOp.addOperation(
            new BinaryOp(LOC, lhs.getValue(), rhs.getValue(), BinModeAttr.Mode.MUL), 0);
    var fmt = mainOp.addOperation(new ConstantOp(LOC, "6 * 7 = %d\n"), 0);
    mainOp.addOperation(
        new PrintOp(LOC, fmt.getValue(), product.getOutputValue().orElseThrow()), 0);
    mainOp.addOperation(new ReturnOp(LOC), 0);

    runProgram(programOp, "6 * 7 = 42\n");
  }

  /**
   * Tests several BinaryOp modes (ADD, SUB, DIV, MOD) with constant operands and prints all
   * results.
   */
  @Test
  void binaryOpArithmeticSuiteTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    var a = mainOp.addOperation(new ConstantOp(LOC, 20), 0);
    var b = mainOp.addOperation(new ConstantOp(LOC, 3), 0);

    var sum =
        mainOp.addOperation(new BinaryOp(LOC, a.getValue(), b.getValue(), BinModeAttr.Mode.ADD), 0);
    var diff =
        mainOp.addOperation(new BinaryOp(LOC, a.getValue(), b.getValue(), BinModeAttr.Mode.SUB), 0);
    var quot =
        mainOp.addOperation(new BinaryOp(LOC, a.getValue(), b.getValue(), BinModeAttr.Mode.DIV), 0);
    var rem =
        mainOp.addOperation(new BinaryOp(LOC, a.getValue(), b.getValue(), BinModeAttr.Mode.MOD), 0);

    var fmtSum = mainOp.addOperation(new ConstantOp(LOC, "20 + 3 = %d\n"), 0);
    var fmtDiff = mainOp.addOperation(new ConstantOp(LOC, "20 - 3 = %d\n"), 0);
    var fmtQuot = mainOp.addOperation(new ConstantOp(LOC, "20 / 3 = %d\n"), 0);
    var fmtRem = mainOp.addOperation(new ConstantOp(LOC, "20 %% 3 = %d\n"), 0);

    mainOp.addOperation(new PrintOp(LOC, fmtSum.getValue(), sum.getOutputValue().orElseThrow()), 0);
    mainOp.addOperation(
        new PrintOp(LOC, fmtDiff.getValue(), diff.getOutputValue().orElseThrow()), 0);
    mainOp.addOperation(
        new PrintOp(LOC, fmtQuot.getValue(), quot.getOutputValue().orElseThrow()), 0);
    mainOp.addOperation(new PrintOp(LOC, fmtRem.getValue(), rem.getOutputValue().orElseThrow()), 0);
    mainOp.addOperation(new ReturnOp(LOC), 0);

    runProgram(programOp, "20 + 3 = 23\n20 - 3 = 17\n20 / 3 = 6\n20 % 3 = 2\n");
  }

  /**
   * Tests CompareOp with constant integer operands: compares 10 &gt; 5 (true) and 3 &gt; 8 (false)
   * and prints the boolean results.
   */
  @Test
  void compareOpConstantsTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    var ten = mainOp.addOperation(new ConstantOp(LOC, 10), 0);
    var five = mainOp.addOperation(new ConstantOp(LOC, 5), 0);
    var three = mainOp.addOperation(new ConstantOp(LOC, 3), 0);
    var eight = mainOp.addOperation(new ConstantOp(LOC, 8), 0);

    var cmpGt =
        mainOp.addOperation(
            new CompareOp(LOC, ten.getValue(), five.getValue(), CompModeAttr.Mode.GT), 0);
    var cmpLt =
        mainOp.addOperation(
            new CompareOp(LOC, three.getValue(), eight.getValue(), CompModeAttr.Mode.LT), 0);
    var cmpEq =
        mainOp.addOperation(
            new CompareOp(LOC, five.getValue(), five.getValue(), CompModeAttr.Mode.EQ), 0);

    var fmtGt = mainOp.addOperation(new ConstantOp(LOC, "10 > 5: %d\n"), 0);
    var fmtLt = mainOp.addOperation(new ConstantOp(LOC, "3 < 8: %d\n"), 0);
    var fmtEq = mainOp.addOperation(new ConstantOp(LOC, "5 == 5: %d\n"), 0);

    mainOp.addOperation(
        new PrintOp(LOC, fmtGt.getValue(), cmpGt.getOutputValue().orElseThrow()), 0);
    mainOp.addOperation(
        new PrintOp(LOC, fmtLt.getValue(), cmpLt.getOutputValue().orElseThrow()), 0);
    mainOp.addOperation(
        new PrintOp(LOC, fmtEq.getValue(), cmpEq.getOutputValue().orElseThrow()), 0);
    mainOp.addOperation(new ReturnOp(LOC), 0);

    runProgram(programOp, "10 > 5: 1\n3 < 8: 1\n5 == 5: 1\n");
  }

  /**
   * Tests BinaryOp with two integers read from the console. The program reads two INT32 values,
   * adds them, and prints the sum.
   */
  @Test
  void binaryOpConsoleInputAddTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    var a = mainOp.addOperation(new ConsoleInOp(LOC, IntegerT.INT32), 0);
    var b = mainOp.addOperation(new ConsoleInOp(LOC, IntegerT.INT32), 0);
    var sum =
        mainOp.addOperation(
            new BinaryOp(LOC, a.getResult(), b.getResult(), BinModeAttr.Mode.ADD), 0);
    var fmt = mainOp.addOperation(new ConstantOp(LOC, "Sum: %d\n"), 0);
    mainOp.addOperation(new PrintOp(LOC, fmt.getValue(), sum.getOutputValue().orElseThrow()), 0);
    mainOp.addOperation(new ReturnOp(LOC), 0);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("13\n29\n".getBytes(UTF_8)));
    runProgram(programOp, "Sum: 42\n");
  }

  /**
   * Tests CompareOp with two integers read from the console. The program reads two INT32 values,
   * compares them with GE (greater-or-equal), and branches to print an appropriate message.
   *
   * <p>Tested with both orderings to exercise both branches.
   */
  @Test
  void compareOpConsoleInputBranchTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block geBlock = mainOp.addBlock(new Block());
    Block ltBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Read two integers, compare a >= b
    var a = entryBlock.addOperation(new ConsoleInOp(LOC, IntegerT.INT32));
    var b = entryBlock.addOperation(new ConsoleInOp(LOC, IntegerT.INT32));
    var cmp =
        entryBlock.addOperation(
            new CompareOp(LOC, a.getResult(), b.getResult(), CompModeAttr.Mode.GE));
    entryBlock.addOperation(
        new BranchCondOp(LOC, cmp.getOutputValue().orElseThrow(), geBlock, ltBlock));

    // a >= b branch
    var geMsg = geBlock.addOperation(new ConstantOp(LOC, "first is greater or equal\n"));
    geBlock.addOperation(new PrintOp(LOC, geMsg.getValue()));
    geBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // a < b branch
    var ltMsg = ltBlock.addOperation(new ConstantOp(LOC, "first is smaller\n"));
    ltBlock.addOperation(new PrintOp(LOC, ltMsg.getValue()));
    ltBlock.addOperation(new BranchOp(LOC, mergeBlock));

    mergeBlock.addOperation(new ReturnOp(LOC));

    VM vm = createVm(programOp);
    // 10 >= 3 → true
    {
      ConsoleInRunner.setInputStream(new ByteArrayInputStream("10\n3\n".getBytes(UTF_8)));
      runVM(vm, "first is greater or equal\n");
    }
    resetOutput();
    // 2 >= 9 → false
    {
      ConsoleInRunner.setInputStream(new ByteArrayInputStream("2\n9\n".getBytes(UTF_8)));
      runVM(vm, "first is smaller\n");
    }
  }

  /**
   * Tests combining BinaryOp and CompareOp with console input. The program reads two integers,
   * computes their product, then checks whether the product exceeds a constant threshold (100), and
   * prints a message accordingly.
   */
  @Test
  void binaryOpAndCompareOpWithConsoleInputTest() {
    ProgramOp programOp = new ProgramOp(LOC);
    FuncOp mainOp = programOp.addOperation(new FuncOp(LOC, "main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block bigBlock = mainOp.addBlock(new Block());
    Block smallBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Read a and b, compute product, compare product > 100
    var a = entryBlock.addOperation(new ConsoleInOp(LOC, IntegerT.INT32));
    var b = entryBlock.addOperation(new ConsoleInOp(LOC, IntegerT.INT32));
    var product =
        entryBlock.addOperation(
            new BinaryOp(LOC, a.getResult(), b.getResult(), BinModeAttr.Mode.MUL));
    var threshold = entryBlock.addOperation(new ConstantOp(LOC, 100));
    var cmp =
        entryBlock.addOperation(
            new CompareOp(
                LOC,
                product.getOutputValue().orElseThrow(),
                threshold.getValue(),
                CompModeAttr.Mode.GT));
    entryBlock.addOperation(
        new BranchCondOp(LOC, cmp.getOutputValue().orElseThrow(), bigBlock, smallBlock));

    // product > 100
    var fmtBig = bigBlock.addOperation(new ConstantOp(LOC, "Product %d exceeds 100!\n"));
    bigBlock.addOperation(
        new PrintOp(LOC, fmtBig.getValue(), product.getOutputValue().orElseThrow()));
    bigBlock.addOperation(new BranchOp(LOC, mergeBlock));

    // product <= 100
    var fmtSmall = smallBlock.addOperation(new ConstantOp(LOC, "Product %d is within 100.\n"));
    smallBlock.addOperation(
        new PrintOp(LOC, fmtSmall.getValue(), product.getOutputValue().orElseThrow()));
    smallBlock.addOperation(new BranchOp(LOC, mergeBlock));

    mergeBlock.addOperation(new ReturnOp(LOC));

    VM vm = createVm(programOp);
    // 11 * 11 = 121 > 100
    {
      ConsoleInRunner.setInputStream(new ByteArrayInputStream("11\n11\n".getBytes(UTF_8)));
      runVM(vm, "Product 121 exceeds 100!\n");
    }
    resetOutput();
    // 5 * 4 = 20 <= 100
    {
      ConsoleInRunner.setInputStream(new ByteArrayInputStream("5\n4\n".getBytes(UTF_8)));
      runVM(vm, "Product 20 is within 100.\n");
    }
  }
}

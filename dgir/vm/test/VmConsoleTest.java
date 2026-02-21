import core.Dialect;
import core.ir.Block;
import dgir.vm.api.OpRunnerRegistry;
import dgir.vm.api.VM;
import dgir.vm.dialect.io.ConsoleInRunner;
import dgir.vm.dialect.io.PrintRunner;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.builtin.types.IntegerT;
import dialect.builtin.types.StringT;
import dialect.cf.BranchCondOp;
import dialect.cf.BranchOp;
import dialect.func.CallOp;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.func.types.FuncType;
import dialect.builtin.types.FloatT;
import dialect.io.ConsoleInOp;
import dialect.io.PrintOp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Testcases for the VM, only testing output from and to the console. */
public class VmConsoleTest {
  private ByteArrayOutputStream output;
  private final boolean printToConsole = true;

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
    OpRunnerRegistry.registerAllRunners();
  }

  @BeforeEach
  void resetOutput() {
    output = new ByteArrayOutputStream();
    PrintRunner.out = new PrintStream(output);
  }

  @AfterEach
  void restoreOutput() {
    PrintRunner.out = System.out;
  }

  private String capturedOutput() {
    if (printToConsole) {
      System.out.print(this.output);
      System.out.flush();
    }
    return output.toString(StandardCharsets.UTF_8);
  }

  /**
   * Creates a simple dgir program printing "Hello World!" to the console and runs it through the
   * VM.
   */
  @Test
  void helloWorldTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp funcOp = programOp.addOperation(new FuncOp("main"));
    var text = funcOp.addOperation(new ConstantOp("Hello World!\n"), 0);
    funcOp.addOperation(new PrintOp(List.of(text.getValue())), 0);
    funcOp.addOperation(new ReturnOp(), 0);

    VM vm = new VM();
    vm.init(programOp);
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("Hello World!\n") : "Unexpected console output";
  }

  /** Same as helloWorldTest but the string is produced by a function call. */
  @Test
  void helloWorldCallTest() {
    ProgramOp programOp = new ProgramOp();

    FuncOp stringOp =
        programOp.addOperation(new FuncOp("string", new FuncType(List.of(), StringT.INSTANCE)));
    {
      var text = stringOp.addOperation(new ConstantOp("Hello World!\n"), 0);
      stringOp.addOperation(new ReturnOp(text.getValue()), 0);
    }

    {
      FuncOp mainOp = programOp.addOperation(new FuncOp("main"));
      var text = mainOp.addOperation(new CallOp(stringOp), 0);
      mainOp.addOperation(new PrintOp(List.of(text.getOutputValue().orElseThrow())), 0);
      mainOp.addOperation(new ReturnOp(), 0);
    }

    VM vm = new VM();
    vm.init(programOp);
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("Hello World!\n") : "Unexpected console output";
  }

  /**
   * Tests BranchOp: entry block prints "before", branches unconditionally to a second block that
   * prints "after".
   */
  @Test
  void unconditionalBranchTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block afterBlock = mainOp.addBlock(new Block());

    // Entry block: print "before\n", then branch unconditionally
    var before = entryBlock.addOperation(new ConstantOp("before\n"));
    entryBlock.addOperation(new PrintOp(before.getValue()));
    entryBlock.addOperation(new BranchOp(afterBlock));

    // After block: print "after\n", then return
    var after = afterBlock.addOperation(new ConstantOp("after\n"));
    afterBlock.addOperation(new PrintOp(after.getValue()));
    afterBlock.addOperation(new ReturnOp());

    VM vm = new VM();
    vm.init(programOp);
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("before\nafter\n") : "Unexpected output: " + capturedOutput();
  }

  /**
   * Tests BranchCondOp taking the true branch: condition is true, so the "true" block is executed.
   */
  @Test
  void conditionalBranchTrueBranchTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block trueBlock = mainOp.addBlock(new Block());
    Block falseBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: define condition = true, branch conditionally
    var cond = entryBlock.addOperation(new ConstantOp(true));
    entryBlock.addOperation(new BranchCondOp(cond.getValue(), trueBlock, falseBlock));

    // True block: print "yes\n", jump to merge
    var yes = trueBlock.addOperation(new ConstantOp("yes\n"));
    trueBlock.addOperation(new PrintOp(yes.getValue()));
    trueBlock.addOperation(new BranchOp(mergeBlock));

    // False block: print "no\n", jump to merge
    var no = falseBlock.addOperation(new ConstantOp("no\n"));
    falseBlock.addOperation(new PrintOp(no.getValue()));
    falseBlock.addOperation(new BranchOp(mergeBlock));

    // Merge block: return
    mergeBlock.addOperation(new ReturnOp());

    VM vm = new VM();
    vm.init(programOp);
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("yes\n") : "Unexpected output: " + capturedOutput();
  }

  /**
   * Tests BranchCondOp taking the false branch: condition is false, so the "false" block is
   * executed.
   */
  @Test
  void conditionalBranchFalseBranchTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block trueBlock = mainOp.addBlock(new Block());
    Block falseBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: define condition = false, branch conditionally
    var cond = entryBlock.addOperation(new ConstantOp(false));
    entryBlock.addOperation(new BranchCondOp(cond.getValue(), trueBlock, falseBlock));

    // True block: print "yes\n", jump to merge
    var yes = trueBlock.addOperation(new ConstantOp("yes\n"));
    trueBlock.addOperation(new PrintOp(yes.getValue()));
    trueBlock.addOperation(new BranchOp(mergeBlock));

    // False block: print "no\n", jump to merge
    var no = falseBlock.addOperation(new ConstantOp("no\n"));
    falseBlock.addOperation(new PrintOp(no.getValue()));
    falseBlock.addOperation(new BranchOp(mergeBlock));

    // Merge block: return
    mergeBlock.addOperation(new ReturnOp());

    VM vm = new VM();
    vm.init(programOp);
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("no\n") : "Unexpected output: " + capturedOutput();
  }

  /**
   * Tests BranchCondOp where the condition is provided by a function call. A helper function
   * returns a boolean constant; the main function uses it as a branch condition.
   */
  @Test
  void conditionalBranchFromFunctionCallTest() {
    ProgramOp programOp = new ProgramOp();

    // Helper function: returns true (int1)
    FuncOp condFunc =
        programOp.addOperation(new FuncOp("getCondition", new FuncType(List.of(), IntegerT.BOOL)));
    {
      var t = condFunc.addOperation(new ConstantOp(true), 0);
      condFunc.addOperation(new ReturnOp(t.getValue()), 0);
    }

    // Main function
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block trueBlock = mainOp.addBlock(new Block());
    Block falseBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: call getCondition(), branch on its result
    var callResult = entryBlock.addOperation(new CallOp(condFunc));
    entryBlock.addOperation(
        new BranchCondOp(callResult.getOutputValue().orElseThrow(), trueBlock, falseBlock));

    // True block
    var yes = trueBlock.addOperation(new ConstantOp("condition true\n"));
    trueBlock.addOperation(new PrintOp(yes.getValue()));
    trueBlock.addOperation(new BranchOp(mergeBlock));

    // False block
    var no = falseBlock.addOperation(new ConstantOp("condition false\n"));
    falseBlock.addOperation(new PrintOp(no.getValue()));
    falseBlock.addOperation(new BranchOp(mergeBlock));

    // Merge block: return
    mergeBlock.addOperation(new ReturnOp());

    VM vm = new VM();
    vm.init(programOp);
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("condition true\n") : "Unexpected output: " + capturedOutput();
  }

  /**
   * Tests a function call where the callee itself uses BranchCondOp internally to decide what to
   * print.
   */
  @Test
  void functionCallWithInternalBranchTest() {
    ProgramOp programOp = new ProgramOp();

    // Helper function: takes an int1 parameter and prints "positive\n" or "non-positive\n"
    FuncOp printFunc =
        programOp.addOperation(
            new FuncOp("printBranch", new FuncType(List.of(IntegerT.BOOL), null)));
    {
      Block funcEntry = printFunc.getEntryBlock();
      Block posBlock = printFunc.addBlock(new Block());
      Block negBlock = printFunc.addBlock(new Block());
      Block retBlock = printFunc.addBlock(new Block());

      // Branch on the parameter
      var param = printFunc.getArgument(0).orElseThrow();
      funcEntry.addOperation(new BranchCondOp(param, posBlock, negBlock));

      {
        var posText = posBlock.addOperation(new ConstantOp("positive\n"));
        posBlock.addOperation(new PrintOp(posText.getValue()));
        posBlock.addOperation(new BranchOp(retBlock));
      }

      {
        var negText = negBlock.addOperation(new ConstantOp("non-positive\n"));
        negBlock.addOperation(new PrintOp(negText.getValue()));
        negBlock.addOperation(new BranchOp(retBlock));
      }

      retBlock.addOperation(new ReturnOp());
    }

    // Main: call printBranch(true) then printBranch(false)
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));
    {
      var trueVal = mainOp.addOperation(new ConstantOp(true), 0);
      var falseVal = mainOp.addOperation(new ConstantOp(false), 0);
      mainOp.addOperation(new CallOp(printFunc, trueVal.getValue()), 0);
      mainOp.addOperation(new CallOp(printFunc, falseVal.getValue()), 0);
      mainOp.addOperation(new ReturnOp(), 0);
    }

    VM vm = new VM();
    vm.init(programOp);
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("positive\nnon-positive\n")
        : "Unexpected output: " + capturedOutput();
  }

  /**
   * Test if console inputs can be used to direct the control flow of the program. The program reads
   * a line from the console, and branches on whether the input is "yes" or "no". The test provides
   * "yes" as input and checks if the correct branch is taken.
   */
  @Test
  void consoleInputBranchTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block yesBlock = mainOp.addBlock(new Block());
    Block noBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: read input from console, compare to "yes", branch conditionally
    var input = entryBlock.addOperation(new ConsoleInOp(IntegerT.BOOL));
    entryBlock.addOperation(new BranchCondOp(input.getResult(), yesBlock, noBlock));

    // Yes block: print "You said true!\n", jump to merge
    var yesText = yesBlock.addOperation(new ConstantOp("You said true!\n"));
    yesBlock.addOperation(new PrintOp(yesText.getValue()));
    yesBlock.addOperation(new BranchOp(mergeBlock));

    // No block: print "You said false!\n", jump to merge
    var noText = noBlock.addOperation(new ConstantOp("You said false!\n"));
    noBlock.addOperation(new PrintOp(noText.getValue()));
    noBlock.addOperation(new BranchOp(mergeBlock));

    // Merge block: return
    mergeBlock.addOperation(new ReturnOp());

    VM vm = new VM();
    vm.init(programOp);

    // Test positive case
    {
      String simulatedInput = "1\n";
      ConsoleInRunner.setInputStream(new ByteArrayInputStream(simulatedInput.getBytes()));
      assert vm.run() : "Program did not terminate successfully.";
      assert capturedOutput().equals("You said true!\n") : "Unexpected output: " + capturedOutput();
    }
    resetOutput();
    // Test negative case
    {
      String simulatedInput = "0\n";
      ConsoleInRunner.setInputStream(new ByteArrayInputStream(simulatedInput.getBytes()));
      assert vm.run() : "Program did not terminate successfully.";
      assert capturedOutput().equals("You said false!\n")
          : "Unexpected output: " + capturedOutput();
    }
  }

  /**
   * Tests reading an integer (INT32) from the console and printing it back to the console. The
   * program reads one integer and prints it with a newline.
   */
  @Test
  void consoleInputReadIntTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    // Read an INT32 from the console
    var input = mainOp.addOperation(new ConsoleInOp(IntegerT.INT32), 0);
    // Print it back
    mainOp.addOperation(new PrintOp(input.getResult()), 0);
    mainOp.addOperation(new ReturnOp(), 0);

    VM vm = new VM();
    vm.init(programOp);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("42\n".getBytes()));
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("42") : "Unexpected output: " + capturedOutput();
  }

  /**
   * Tests reading a string from the console and printing it back to the console. The program reads
   * one line of text and echoes it.
   */
  @Test
  void consoleInputReadStringTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    // Read a String from the console
    var input = mainOp.addOperation(new ConsoleInOp(StringT.INSTANCE), 0);
    // Print it back
    mainOp.addOperation(new PrintOp(input.getResult()), 0);
    mainOp.addOperation(new ReturnOp(), 0);

    VM vm = new VM();
    vm.init(programOp);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("Hello from stdin\n".getBytes()));
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("Hello from stdin") : "Unexpected output: " + capturedOutput();
  }

  /**
   * Tests formatted printing using a string read from the console. The program reads a name from
   * the console and prints a greeting using printf-style formatting.
   */
  @Test
  void consoleInputFormattedPrintStringTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    // Format string constant
    var fmt = mainOp.addOperation(new ConstantOp("Hello, %s!\n"), 0);
    // Read the name from the console
    var name = mainOp.addOperation(new ConsoleInOp(StringT.INSTANCE), 0);
    // Print formatted: "Hello, <name>!\n"
    mainOp.addOperation(new PrintOp(fmt.getValue(), name.getResult()), 0);
    mainOp.addOperation(new ReturnOp(), 0);

    VM vm = new VM();
    vm.init(programOp);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("World\n".getBytes()));
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("Hello, World!\n") : "Unexpected output: " + capturedOutput();
  }

  /**
   * Tests formatted printing using an integer read from the console. The program reads a number
   * from the console and prints it inside a sentence using printf-style formatting.
   */
  @Test
  void consoleInputFormattedPrintIntTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    // Format string constant
    var fmt = mainOp.addOperation(new ConstantOp("The answer is %d.\n"), 0);
    // Read an INT32 from the console
    var number = mainOp.addOperation(new ConsoleInOp(IntegerT.INT32), 0);
    // Print formatted: "The answer is <number>.\n"
    mainOp.addOperation(new PrintOp(fmt.getValue(), number.getResult()), 0);
    mainOp.addOperation(new ReturnOp(), 0);

    VM vm = new VM();
    vm.init(programOp);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("42\n".getBytes()));
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("The answer is 42.\n")
        : "Unexpected output: " + capturedOutput();
  }

  /**
   * Tests formatted printing using a float read from the console. The program reads a
   * floating-point number and prints it with two decimal places using printf-style formatting.
   */
  @Test
  void consoleInputFormattedPrintFloatTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    // Format string constant
    var fmt = mainOp.addOperation(new ConstantOp("Pi is approximately %.2f.\n"), 0);
    // Read a FLOAT32 from the console
    var number = mainOp.addOperation(new ConsoleInOp(FloatT.FLOAT32), 0);
    // Print formatted: "Pi is approximately <number>.\n"
    mainOp.addOperation(new PrintOp(fmt.getValue(), number.getResult()), 0);
    mainOp.addOperation(new ReturnOp(), 0);

    VM vm = new VM();
    vm.init(programOp);

    ConsoleInRunner.setInputStream(new ByteArrayInputStream("3.14\n".getBytes()));
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("Pi is approximately 3.14.\n")
        : "Unexpected output: " + capturedOutput();
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
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block trueBlock = mainOp.addBlock(new Block());
    Block falseBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: read a bool from console, branch on it
    var flag = entryBlock.addOperation(new ConsoleInOp(IntegerT.BOOL));
    entryBlock.addOperation(new BranchCondOp(flag.getResult(), trueBlock, falseBlock));

    // True block: read an integer from console, print it formatted, jump to merge
    var fmt = trueBlock.addOperation(new ConstantOp("You entered: %d\n"));
    var n = trueBlock.addOperation(new ConsoleInOp(IntegerT.INT32));
    trueBlock.addOperation(new PrintOp(fmt.getValue(), n.getResult()));
    trueBlock.addOperation(new BranchOp(mergeBlock));

    // False block: print a fixed message without any console input, jump to merge
    var noInput = falseBlock.addOperation(new ConstantOp("No input given.\n"));
    falseBlock.addOperation(new PrintOp(noInput.getValue()));
    falseBlock.addOperation(new BranchOp(mergeBlock));

    // Merge block: return
    mergeBlock.addOperation(new ReturnOp());

    VM vm = new VM();
    vm.init(programOp);

    // Test true branch: flag=1 -> read integer 7 -> print "You entered: 7\n"
    {
      ConsoleInRunner.setInputStream(new ByteArrayInputStream("1\n7\n".getBytes()));
      assert vm.run() : "Program did not terminate successfully.";
      assert capturedOutput().equals("You entered: 7\n") : "Unexpected output: " + capturedOutput();
    }
    resetOutput();
    // Test false branch: flag=0 -> no further console read -> print "No input given.\n"
    {
      ConsoleInRunner.setInputStream(new ByteArrayInputStream("0\n".getBytes()));
      assert vm.run() : "Program did not terminate successfully.";
      assert capturedOutput().equals("No input given.\n")
          : "Unexpected output: " + capturedOutput();
    }
  }

  /**
   * Tests reading multiple values from the console — an integer and a string — and printing them
   * together using printf-style format string with two substitutions.
   */
  @Test
  void consoleInputFormattedPrintMultipleValuesTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    // Format string constant
    var fmt = mainOp.addOperation(new ConstantOp("%s scored %d points.\n"), 0);
    // Read a name (string) then a score (int)
    var nameIn = mainOp.addOperation(new ConsoleInOp(StringT.INSTANCE), 0);
    var scoreIn = mainOp.addOperation(new ConsoleInOp(IntegerT.INT32), 0);
    // Print formatted
    mainOp.addOperation(new PrintOp(fmt.getValue(), nameIn.getResult(), scoreIn.getResult()), 0);
    mainOp.addOperation(new ReturnOp(), 0);

    VM vm = new VM();
    vm.init(programOp);

    // Two consecutive lines: first the name, then the score
    ConsoleInRunner.setInputStream(new ByteArrayInputStream("Alice\n100\n".getBytes()));
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("Alice scored 100 points.\n")
        : "Unexpected output: " + capturedOutput();
  }
}

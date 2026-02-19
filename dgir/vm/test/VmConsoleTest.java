import core.Dialect;
import core.ir.Block;
import dgir.vm.api.OpRunnerRegistry;
import dgir.vm.api.VM;
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
import dialect.io.PrintOp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Testcases for the VM, only testing output from and to the console.
 */
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
   * Creates a simple dgir program printing "Hello World!" to the console and runs it through the VM.
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

  /**
   * Same as helloWorldTest but the string is produced by a function call.
   */
  @Test
  void helloWorldCallTest() {
    ProgramOp programOp = new ProgramOp();

    FuncOp stringOp = programOp.addOperation(new FuncOp("string", new FuncType(List.of(), StringT.INSTANCE)));
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
   * Tests BranchOp: entry block prints "before", branches unconditionally to a second block that prints "after".
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
    Block trueBlock  = mainOp.addBlock(new Block());
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
   * Tests BranchCondOp taking the false branch: condition is false, so the "false" block is executed.
   */
  @Test
  void conditionalBranchFalseBranchTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block trueBlock  = mainOp.addBlock(new Block());
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
   * Tests BranchCondOp where the condition is provided by a function call.
   * A helper function returns a boolean constant; the main function uses it as a branch condition.
   */
  @Test
  void conditionalBranchFromFunctionCallTest() {
    ProgramOp programOp = new ProgramOp();

    // Helper function: returns true (int1)
    FuncOp condFunc = programOp.addOperation(new FuncOp("getCondition", new FuncType(List.of(), IntegerT.BOOL)));
    {
      var t = condFunc.addOperation(new ConstantOp(true), 0);
      condFunc.addOperation(new ReturnOp(t.getValue()), 0);
    }

    // Main function
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));

    Block entryBlock = mainOp.getEntryBlock();
    Block trueBlock  = mainOp.addBlock(new Block());
    Block falseBlock = mainOp.addBlock(new Block());
    Block mergeBlock = mainOp.addBlock(new Block());

    // Entry: call getCondition(), branch on its result
    var callResult = entryBlock.addOperation(new CallOp(condFunc));
    entryBlock.addOperation(new BranchCondOp(callResult.getOutputValue().orElseThrow(), trueBlock, falseBlock));

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
   * Tests a function call where the callee itself uses BranchCondOp internally to decide what to print.
   */
  @Test
  void functionCallWithInternalBranchTest() {
    ProgramOp programOp = new ProgramOp();

    // Helper function: takes an int1 parameter and prints "positive\n" or "non-positive\n"
    FuncOp printFunc = programOp.addOperation(new FuncOp("printBranch", new FuncType(List.of(IntegerT.BOOL), null)));
    {
      Block funcEntry = printFunc.getEntryBlock();
      Block posBlock  = printFunc.addBlock(new Block());
      Block negBlock  = printFunc.addBlock(new Block());
      Block retBlock  = printFunc.addBlock(new Block());

      // Branch on the parameter
      var param = printFunc.getArgument(0);
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
      var trueVal  = mainOp.addOperation(new ConstantOp(true),  0);
      var falseVal = mainOp.addOperation(new ConstantOp(false), 0);
      mainOp.addOperation(new CallOp(printFunc, trueVal.getValue()),  0);
      mainOp.addOperation(new CallOp(printFunc, falseVal.getValue()), 0);
      mainOp.addOperation(new ReturnOp(), 0);
    }

    VM vm = new VM();
    vm.init(programOp);
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("positive\nnon-positive\n") : "Unexpected output: " + capturedOutput();
  }
}

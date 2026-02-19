import core.Dialect;
import dgir.vm.api.OpRunnerRegistry;
import dgir.vm.api.VM;
import dgir.vm.dialect.io.PrintRunner;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.builtin.types.StringT;
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
}

import core.Dialect;
import dgir.vm.api.OpRunnerRegistry;
import dgir.vm.api.VM;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.io.PrintOp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Testcases for the VM, only testing output from and to the console.
 */
public class VmConsoleTest {
  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
    OpRunnerRegistry.registerAllRunners();
  }

  /**
   * Creates a simple dgir program printing "Hello World!" to the console and runs it through the VM.
   */
  @Test
  void helloWorldTest() {
    ProgramOp programOp = new ProgramOp();
    FuncOp funcOp = programOp.addOperation(new FuncOp("main"));
    var text = funcOp.addOperation(new ConstantOp("Hello World!\n"), 0);
    funcOp.addOperation(new PrintOp(List.of(text.getOutputValue())), 0);
    funcOp.addOperation(new ReturnOp(), 0);

    VM vm = new VM();
    vm.init(programOp);
    assert vm.run() : "Program did not terminate successfully.";
  }
}

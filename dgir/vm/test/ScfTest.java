import core.Dialect;
import dgir.vm.api.OpRunnerRegistry;
import dgir.vm.api.VM;
import dgir.vm.dialect.io.PrintRunner;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.io.PrintOp;
import dialect.scf.ContinueOp;
import dialect.scf.ForOp;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.*;

public class ScfTest {
  private ByteArrayOutputStream output;

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

  @AfterAll
  static void restoreOutput() {
    PrintRunner.out = System.out;
  }

  private String capturedOutput() {
    return output.toString(StandardCharsets.UTF_8);
  }

  /** Probe: for(i=0; i<3; i++) print(i) -- verify the actual output produced. */
  @Test
  void basicForLoopOutput() {
    ProgramOp programOp = new ProgramOp();
    FuncOp mainOp = programOp.addOperation(new FuncOp("main"));
    {
      var init = mainOp.addOperation(new ConstantOp(0), 0);
      var lower = mainOp.addOperation(new ConstantOp(0), 0);
      var upper = mainOp.addOperation(new ConstantOp(3), 0);
      var step = mainOp.addOperation(new ConstantOp(1), 0);
      var format = mainOp.addOperation(new ConstantOp("%d\n"), 0);

      ForOp forOp =
          mainOp.addOperation(
              new ForOp(init.getValue(), lower.getValue(), upper.getValue(), step.getValue()), 0);
      {
        forOp.getEntryBlock().addOperation(new PrintOp(format.getValue(), forOp.getInductionValue()));
        forOp.getEntryBlock().addOperation(new ContinueOp());
      }

      mainOp.addOperation(new ReturnOp(), 0);
    }

    VM vm = new VM();
    vm.init(programOp);
    assert vm.run() : "Program did not terminate successfully.";
    assert capturedOutput().equals("0\n1\n2\n") : "Unexpected output: " + capturedOutput();
  }
}

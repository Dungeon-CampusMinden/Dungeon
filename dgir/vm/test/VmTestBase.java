import core.Dialect;
import core.serialization.Utils;
import dgir.vm.api.OpRunnerRegistry;
import dgir.vm.api.VM;
import dgir.vm.dialect.io.PrintRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static dialect.builtin.BuiltinOps.ProgramOp;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VmTestBase {
  public ByteArrayOutputStream output;
  public final boolean printToConsole = true;
  public static boolean printIr = false;
  public static ObjectMapper mapper = Utils.getMapper(true);

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
    OpRunnerRegistry.registerAllRunners();

  }

  @BeforeEach
  public void resetOutput() {
    output = new ByteArrayOutputStream();
    PrintRunner.out = new PrintStream(output);
  }

  @AfterEach
  public void restoreOutput() {
    PrintRunner.out = System.out;
  }

  public String out() {
    return output.toString(StandardCharsets.UTF_8);
  }

  public static VM createVm(ProgramOp program) {
    VM vm = new VM();
    vm.init(program);
    return vm;
  }

  public void runProgram(ProgramOp program, String expectedOutput) {
    var vm = createVm(program);
    runVM(vm, expectedOutput);
  }

  public void runVM(VM vm, String expectedOutput) {
    assertTrue(vm.run(), "Program did not terminate successfully");
    if (printToConsole) {
      System.out.print(this.output);
      System.out.flush();
    }

    checkOutput(expectedOutput);
  }

  public void checkOutput(String expectedOutput) {
    assertEquals(
        expectedOutput,
        this.out(),
        "Program output did not match expected output\n\n"
            + mapper.writeValueAsString(expectedOutput));
    if (printIr) {
      System.out.println(mapper.writeValueAsString(expectedOutput));
    }
  }
}

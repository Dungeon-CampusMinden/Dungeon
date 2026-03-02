import static dialect.builtin.BuiltinOps.ProgramOp;

import compiler.java.JavaCompiler;
import core.serialization.Utils;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CompilerTest {
  @Test
  void variableAssignment() {
    String code =
"""
public class SimpleAssignment {
  public static void main() {
    int x = 5;
    x = 10;
  }
}
""";
    Optional<ProgramOp> programOp = JavaCompiler.compileSource(code, "SimpleAssignment.java");
    assert programOp.isPresent() : "Compilation failed";
    System.out.println(Utils.getMapper(true).writeValueAsString(programOp.get()));
    assert programOp.get().verify(true) : "Verification failed";
  }
}

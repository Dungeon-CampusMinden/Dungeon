import blockly.dgir.compiler.java.JavaCompiler;
import blockly.dgir.dialect.dg.DungeonDialect;
import blockly.dgir.vm.dialect.dg.DungeonDialectRunner;
import dgir.core.Dialect;
import dgir.core.serialization.Utils;
import dgir.dialect.builtin.BuiltinOps;
import dgir.vm.api.DialectRunner;
import dgir.vm.api.OpRunnerRegistry;
import dgir.vm.api.VM;
import org.junit.jupiter.api.BeforeAll;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CompilerTestBase {
  public static boolean printSource = false;
  public static boolean saveSource = true;
  public static boolean printResult = true;
  public static boolean saveResult = true;
  public static String savePath = "test_results/";
  public static VM vm = new VM();

  @BeforeAll
  public static void setup() {
    Dialect.registerAllDialects();
    new DungeonDialect().register();
    DialectRunner.registerAllDialects();
    OpRunnerRegistry.registerDialectRunner(new DungeonDialectRunner());
  }

  public static void testSource(String source) {
    String callerName = dgir.core.Utils.getCallingMethodName();
    String formatedCode = source.replace("%ClassName", callerName);

    // Ensure the output directory exists before writing files
    try {
      Files.createDirectories(Paths.get(savePath));
      copyDirectoryRecursively(Paths.get("test_assets/vscode-config"), Paths.get(savePath));
    } catch (IOException e) {
      System.out.println("Failed to create output directory '" + savePath + "': " + e);
    }

    Optional<BuiltinOps.ProgramOp> programOp =
        JavaCompiler.compileSource(formatedCode, callerName + ".java");
    assert programOp.isPresent() : "Compilation failed";

    if (printSource) System.out.println(formatedCode);
    if (saveSource) {
      String filePath = savePath + callerName + ".java";
      try {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), UTF_8);
        writer.write(formatedCode);
        writer.close();
        System.out.println("Saved source to " + filePath);
      } catch (IOException e) {
        System.out.println("Failed to save source to " + filePath + ": " + e);
      }
    }

    String result = Utils.getMapper(true).writeValueAsString(programOp.get());
    assert programOp.get().verify(true) : "Verification failed\n" + result;

    if (printResult) System.out.println(result);
    if (saveResult) {
      String filePath = savePath + callerName + ".json";
      try {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), UTF_8);
        writer.write(result);
        writer.close();
        System.out.println("Saved result to " + filePath);
      } catch (IOException e) {
        System.out.println("Failed to save result to " + filePath + ": " + e);
      }
    }

    vm.init(programOp.get());
    try {
      assert vm.run() : "Execution failed";
    } catch (Exception e) {
      throw new RuntimeException("Execution failed", e);
    }
  }

  protected static void copyDirectoryRecursively(Path source, Path target) throws IOException {
    if (!Files.exists(source)) {
      return;
    }
    try (var paths = Files.walk(source)) {
      paths.forEach(
          path -> {
            Path relative = source.relativize(path);
            Path destination = target.resolve(relative);
            try {
              if (Files.isDirectory(path)) {
                Files.createDirectories(destination);
              } else {
                Files.createDirectories(destination.getParent());
                Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
              }
            } catch (IOException ioException) {
              throw new UncheckedIOException(ioException);
            }
          });
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }
}

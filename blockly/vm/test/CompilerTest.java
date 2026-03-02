import static dialect.builtin.BuiltinOps.ProgramOp;
import static java.nio.charset.StandardCharsets.UTF_8;

import compiler.java.JavaCompiler;
import core.serialization.Utils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class CompilerTest {
  public static boolean printSource = false;
  public static boolean saveSource = true;
  public static boolean printResult = true;
  public static boolean saveResult = true;
  public static String savePath = "test_results/";

  public static void testSource(String source) {
    String callerName = core.Utils.getCallingMethodName();
    String formatedCode = source.replace("%ClassName", callerName);

    // Ensure the output directory exists before writing files
    try {
      Files.createDirectories(Paths.get(savePath));
      copyDirectoryRecursively(Paths.get("test_assets/vscode-config"), Paths.get(savePath));
    } catch (IOException e) {
      System.out.println("Failed to create output directory '" + savePath + "': " + e);
    }

    Optional<ProgramOp> programOp = JavaCompiler.compileSource(formatedCode, callerName + ".java");
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

    assert programOp.get().verify(true) : "Verification failed";

    String result = Utils.getMapper(true).writeValueAsString(programOp.get());
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
  }

  private static void copyDirectoryRecursively(Path source, Path target) throws IOException {
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

  @Test
  void variableAssignment() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int x = 5;
    x = 10;
  }
}
""";
    testSource(code);
  }

  @Test
  void variableAssignment_binaryExpr() {
    String code =
"""
public class %ClassName {
  public static void main() {
    int a = 5;
    int b = 10;
    int c = a + b;
    int d = c * 2;
    float e = d / 2;
    float f = e - 1;
    float g = f * 2;
    float h = g / 2;
    float i = h % 4;
    int j = 4 + 3;
    float k = 4 + 3f * 2 / 4;
  }
}
""";
    testSource(code);
  }
}

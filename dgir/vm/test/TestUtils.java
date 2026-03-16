import dgir.core.serialization.Utils;
import dgir.dialect.builtin.BuiltinOps;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import tools.jackson.databind.ObjectMapper;

public interface TestUtils {
  @NotNull Optional<Path> TEST_PROGRAMS_DIR = findTestProgramsDir();
  @NotNull ObjectMapper MAPPER = Utils.getMapper(true);

  static Optional<BuiltinOps.ProgramOp> loadProgram(String fileName) {
    try {
      String json =
          loadTestFile(fileName)
              .orElseThrow(() -> new RuntimeException("Test program file not found: " + fileName));
      return Optional.ofNullable(MAPPER.readValue(json, BuiltinOps.ProgramOp.class));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  static @NotNull Path findWorkspaceRoot() {
    Path rootPath = Paths.get("");
    Path dir = rootPath.toAbsolutePath();
    while (dir != null) {
      if (Files.exists(dir.resolve("gradlew"))) {
        return dir;
      }
      dir = dir.getParent();
    }
    return rootPath.toAbsolutePath().getParent().getParent();
  }

  static @NotNull Optional<Path> findDapJar() {
    return Optional.of(findWorkspaceRoot().resolve("dgir/vm/build/libs/dgir-vm-dap.jar"));
  }

  static @NotNull Optional<Path> findTestProgramsDir() {
    Path workspaceRoot = findWorkspaceRoot();
    Path dgirSubdir = workspaceRoot.resolve("dgir/vm/test_assets/DapTestFiles");
    if (Files.isDirectory(dgirSubdir)) {
      return Optional.of(dgirSubdir);
    }

    Path dgirRoot = workspaceRoot.resolve("dgir/vm/test_assets");
    if (Files.isDirectory(dgirRoot)) {
      return Optional.of(dgirRoot);
    }

    return Optional.empty();
  }

  static @NotNull Optional<String> loadTestFile(String fileName) {
    return TEST_PROGRAMS_DIR.flatMap(dir -> loadTestFile(dir.resolve(fileName)));
  }

  static @NotNull Optional<String> loadTestFile(Path path) {
    try {
      return Optional.of(Files.readString(path, StandardCharsets.UTF_8));
    } catch (IOException e) {
      return Optional.empty();
    }
  }
}

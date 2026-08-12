package wizard.runner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import wizard.runner.report.ProjectValidationReport;
import wizard.runner.room.RoomDeriver;
import wizard.runner.validation.ProjectValidationPipeline;
import wizard.runner.validation.ValidationResult;

/** Focused contract tests for production project validation and its machine report. */
final class ProjectValidationReportTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @TempDir Path temporaryDirectory;

  @Test
  void validProjectCanBeDerivedAndReportedWithoutWriting() throws IOException {
    Path project = materializeCanonicalProject("valid-project");
    byte[] deerBefore = Files.readAllBytes(project.resolve("deer.json"));
    byte[] assetBefore = Files.readAllBytes(canonicalAsset(project));

    ValidationResult validation = new ProjectValidationPipeline().validate(project);
    new RoomDeriver().derive(validation);
    JsonNode report = report(validation);

    assertTrue(report.required("valid").booleanValue());
    assertEquals("0.4", report.required("runnerVersion").stringValue());
    assertEquals(64, report.required("rawDeerSha256").stringValue().length());
    assertEquals(64, report.required("hostInputSha256").stringValue().length());
    assertTrue(report.required("issues").isEmpty());
    assertFalse(report.has("command"));
    assertFalse(report.has("generatorVersion"));
    assertArrayEquals(deerBefore, Files.readAllBytes(project.resolve("deer.json")));
    assertArrayEquals(assetBefore, Files.readAllBytes(canonicalAsset(project)));
    assertEquals(2L, regularFileCount(project));
  }

  @Test
  void invalidProjectProducesTheSharedCanonicalReportWithoutWriting() throws IOException {
    Path project = materializeCanonicalProject("invalid-project");
    ObjectNode deer = (ObjectNode) MAPPER.readTree(project.resolve("deer.json").toFile());
    deer.remove("metadata");
    Files.write(
        project.resolve("deer.json"),
        MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(deer));
    byte[] deerBefore = Files.readAllBytes(project.resolve("deer.json"));

    ValidationResult validation = new ProjectValidationPipeline().validate(project);
    String firstReport =
        ProjectValidationReport.from(RunnerVersion.load(), validation).canonicalJson();
    String secondReport =
        ProjectValidationReport.from(RunnerVersion.load(), validation).canonicalJson();
    JsonNode report = MAPPER.readTree(firstReport);

    assertFalse(report.required("valid").booleanValue());
    assertTrue(hasIssue(report, "SCHEMA_INVALID"));
    assertTrue(report.required("rawDeerSha256").isString());
    assertTrue(report.required("hostInputSha256").isNull());
    assertEquals(firstReport, secondReport);
    assertArrayEquals(deerBefore, Files.readAllBytes(project.resolve("deer.json")));
    assertEquals(2L, regularFileCount(project));
  }

  @Test
  void packagingValidatorWritesCanonicalUtf8BytesWithoutTrailingNewline() throws Exception {
    Path project = materializeCanonicalProject("process-project");
    Files.copy(canonicalAsset(project), project.resolve("assets/custom/überflüssig.png"));
    ValidationResult validation = new ProjectValidationPipeline().validate(project);
    String expectedReport =
        ProjectValidationReport.from(RunnerVersion.load(), validation).canonicalJson();
    assertTrue(expectedReport.contains("überflüssig"));

    Process process =
        new ProcessBuilder(
                javaExecutable(),
                "-Dstdout.encoding=windows-1252",
                "-Dslf4j.internal.verbosity=ERROR",
                "-cp",
                System.getProperty("java.class.path"),
                PackagingProjectValidator.class.getName(),
                project.toString())
            .start();
    int exitCode = process.waitFor();
    byte[] standardOutput = process.getInputStream().readAllBytes();
    byte[] standardError = process.getErrorStream().readAllBytes();

    assertEquals(0, exitCode, new String(standardError, StandardCharsets.UTF_8));
    assertArrayEquals(expectedReport.getBytes(StandardCharsets.UTF_8), standardOutput);
    assertFalse(standardOutput[standardOutput.length - 1] == '\n');
    assertFalse(standardOutput[standardOutput.length - 1] == '\r');
    assertEquals("", new String(standardError, StandardCharsets.UTF_8));
  }

  private static JsonNode report(final ValidationResult validation) throws IOException {
    return MAPPER.readTree(
        ProjectValidationReport.from(RunnerVersion.load(), validation).canonicalJson());
  }

  private static String javaExecutable() {
    String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
    return Path.of(System.getProperty("java.home"), "bin", executable).toString();
  }

  private Path materializeCanonicalProject(final String name) throws IOException {
    Path examples = Path.of("examples", "foundation-v0.4").toAbsolutePath().normalize();
    Path project = Files.createDirectory(temporaryDirectory.resolve(name));
    Path assetDirectory = Files.createDirectories(project.resolve("assets/custom"));
    Files.copy(examples.resolve("deer.json"), project.resolve("deer.json"));
    Files.copy(
        examples.resolve("assets/custom/foundation-note-3b50ea522803.png"),
        assetDirectory.resolve("foundation-note-3b50ea522803.png"));
    return project;
  }

  private static Path canonicalAsset(final Path project) {
    return project.resolve("assets/custom/foundation-note-3b50ea522803.png");
  }

  private static boolean hasIssue(final JsonNode report, final String code) {
    return findIssue(report, code).isPresent();
  }

  private static Optional<JsonNode> findIssue(final JsonNode report, final String code) {
    for (JsonNode issue : report.required("issues")) {
      if (code.equals(issue.required("code").stringValue())) {
        return Optional.of(issue);
      }
    }
    return Optional.empty();
  }

  private static long regularFileCount(final Path directory) throws IOException {
    try (var files = Files.walk(directory)) {
      return files.filter(Files::isRegularFile).count();
    }
  }
}

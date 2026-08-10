package wizard.runner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wizard.runner.RunnerRequest.Command;

/** Focused CLI contract tests for the generic validate/host/join application. */
final class RunnerCliValidationTest {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @TempDir Path temporaryDirectory;

  @Test
  void validateUsesProductionDerivationWithoutWriting() throws IOException {
    Path project = materializeCanonicalProject("valid-project");
    byte[] deerBefore = Files.readAllBytes(project.resolve("deer.json"));
    byte[] assetBefore = Files.readAllBytes(canonicalAsset(project));

    CliResult result = run("validate", "--project", project.toString());

    assertEquals(0, result.exitCode(), result.standardOutput());
    assertEquals("", result.standardError());
    JsonNode report = MAPPER.readTree(result.standardOutput());
    assertEquals("validate", report.required("command").textValue());
    assertEquals("0.4", report.required("runnerVersion").textValue());
    assertTrue(report.required("valid").booleanValue());
    assertTrue(report.required("issues").isEmpty());
    assertEquals(64, report.required("hostInputSha256").textValue().length());
    assertFalse(report.has("generatorVersion"));
    assertArrayEquals(deerBefore, Files.readAllBytes(project.resolve("deer.json")));
    assertArrayEquals(assetBefore, Files.readAllBytes(canonicalAsset(project)));
    assertEquals(2L, regularFileCount(project));
  }

  @Test
  void validateReturnsTheSharedInvalidReportWithoutStartingOrWriting() throws IOException {
    Path project = materializeCanonicalProject("invalid-project");
    ObjectNode deer = (ObjectNode) MAPPER.readTree(project.resolve("deer.json").toFile());
    deer.remove("metadata");
    Files.write(
        project.resolve("deer.json"),
        MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(deer));
    byte[] deerBefore = Files.readAllBytes(project.resolve("deer.json"));

    CliResult result = run("validate", "--project", project.toString());

    assertEquals(1, result.exitCode());
    assertEquals("", result.standardError());
    JsonNode report = MAPPER.readTree(result.standardOutput());
    assertFalse(report.required("valid").booleanValue());
    assertTrue(hasIssue(report, "SCHEMA_INVALID"));
    assertArrayEquals(deerBefore, Files.readAllBytes(project.resolve("deer.json")));
    assertEquals(2L, regularFileCount(project));
  }

  @Test
  void routesTheExactThreeCommandsWithOneNormalizedProjectInput() throws IOException {
    Path project = Files.createDirectory(temporaryDirectory.resolve("project"));
    List<RunnerRequest> requests = new ArrayList<>();
    RunnerCli.CommandExecutor executor =
        (request, version) -> {
          requests.add(request);
          return new RunnerResult(0, request.command().token(), "");
        };

    assertEquals(
        "validate", run(executor, "validate", "--project", project.toString()).standardOutput());
    assertEquals("host", run(executor, "host", "--project", project.toString()).standardOutput());
    assertEquals("join", run(executor, "join", "--project", project.toString()).standardOutput());

    assertEquals(
        List.of(Command.VALIDATE, Command.HOST, Command.JOIN),
        requests.stream().map(RunnerRequest::command).toList());
    Path realProject = project.toRealPath();
    assertTrue(requests.stream().allMatch(request -> request.project().equals(realProject)));
  }

  @Test
  void rejectsMissingRepeatedAndMisplacedOptionsBeforeDispatch() {
    AtomicInteger calls = new AtomicInteger();
    RunnerCli.CommandExecutor executor =
        (request, version) -> {
          calls.incrementAndGet();
          return new RunnerResult(0, "unexpected", "");
        };
    List<String[]> invalid =
        List.of(
            new String[] {},
            new String[] {"join"},
            new String[] {"join", "--assets", "assets"},
            new String[] {"host", "--project", "a", "--project", "b"},
            new String[] {"join", "--assets", "a", "--assets", "b"},
            new String[] {"validate", "project"},
            new String[] {"--help", "extra"},
            new String[] {"--version", "extra"});

    for (String[] arguments : invalid) {
      assertEquals(1, run(executor, arguments).exitCode());
    }
    assertEquals(0, calls.get());
  }

  @Test
  void joinWithInvalidProjectFailsDuringProjectValidation() throws IOException {
    Path clientRoot = Files.createDirectory(temporaryDirectory.resolve("client-only"));
    Files.writeString(clientRoot.resolve("deer.json"), "invalid");
    byte[] deerBefore = Files.readAllBytes(clientRoot.resolve("deer.json"));

    CliResult result = run("join", "--project", clientRoot.toString());

    assertEquals(1, result.exitCode());
    JsonNode report = MAPPER.readTree(result.standardOutput());
    assertEquals("join", report.required("command").textValue());
    assertTrue(hasIssue(report, "JSON_PARSE_INVALID"));
    assertTrue(report.required("rawDeerSha256").isTextual());
    assertTrue(report.required("hostInputSha256").isNull());
    assertArrayEquals(deerBefore, Files.readAllBytes(clientRoot.resolve("deer.json")));
  }

  private Path materializeCanonicalProject(final String name) throws IOException {
    Path examples = Path.of("examples", "foundation-v0.4").toAbsolutePath().normalize();
    Path project = Files.createDirectory(temporaryDirectory.resolve(name));
    Path assetDirectory = Files.createDirectories(project.resolve("assets/custom"));
    Files.copy(examples.resolve("deer.json"), project.resolve("deer.json"));
    Files.copy(
        examples.resolve("assets/custom/3b50ea522803-foundation-note.png"),
        assetDirectory.resolve("3b50ea522803-foundation-note.png"));
    return project;
  }

  private static Path canonicalAsset(final Path project) {
    return project.resolve("assets/custom/3b50ea522803-foundation-note.png");
  }

  private static boolean hasIssue(final JsonNode report, final String code) {
    return findIssue(report, code).isPresent();
  }

  private static Optional<JsonNode> findIssue(final JsonNode report, final String code) {
    for (JsonNode issue : report.required("issues")) {
      if (code.equals(issue.required("code").textValue())) {
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

  private static CliResult run(final String... arguments) {
    return run((RunnerCli.CommandExecutor) null, arguments);
  }

  private static CliResult run(
      final RunnerCli.CommandExecutor executor, final String... arguments) {
    ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
    ByteArrayOutputStream standardError = new ByteArrayOutputStream();
    int exitCode;
    try (PrintStream output = new PrintStream(standardOutput, true, StandardCharsets.UTF_8);
        PrintStream error = new PrintStream(standardError, true, StandardCharsets.UTF_8)) {
      exitCode =
          executor == null
              ? RunnerCli.run(arguments, output, error)
              : RunnerCli.run(arguments, output, error, executor);
    }
    return new CliResult(
        exitCode,
        standardOutput.toString(StandardCharsets.UTF_8),
        standardError.toString(StandardCharsets.UTF_8));
  }

  private record CliResult(int exitCode, String standardOutput, String standardError) {}
}

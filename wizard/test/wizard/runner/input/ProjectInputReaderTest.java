package wizard.runner.input;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wizard.runner.contract.ContractCapabilities;
import wizard.runner.contract.IssueCode;

/** Focused hostile-boundary tests for the production project reader. */
final class ProjectInputReaderTest {
  @TempDir Path temporaryDirectory;

  @Test
  void readsExactBytesWithoutMutatingTheProject() throws IOException {
    byte[] deer = "{\"formatVersion\":\"0.5\",\"seed\":42}\n".getBytes(StandardCharsets.UTF_8);
    Path project = createProject(deer);
    byte[] before = Files.readAllBytes(project.resolve("deer.json"));

    InputSnapshot snapshot = new ProjectInputReader().read(project);

    assertTrue(snapshot.issues().isEmpty());
    assertEquals(sha256(deer), snapshot.rawDeerSha256().orElseThrow());
    assertEquals(42L, snapshot.document().orElseThrow().required("seed").longValue());
    assertArrayEquals(before, Files.readAllBytes(project.resolve("deer.json")));
    assertEquals(1L, entryCount(project));
  }

  @Test
  void rejectsMalformedUtf8Deterministically() throws IOException {
    byte[] deer = {(byte) '{', (byte) '"', (byte) 'x', (byte) '"', (byte) ':', (byte) 0xC3};
    Path project = createProject(deer);

    InputSnapshot first = new ProjectInputReader().read(project);
    InputSnapshot second = new ProjectInputReader().read(project);

    assertIssue(first, IssueCode.INPUT_UTF8_INVALID);
    assertEquals(first.issues(), second.issues());
    assertEquals(first.rawDeerSha256(), second.rawDeerSha256());
  }

  @Test
  void rejectsDuplicateMembers() throws IOException {
    byte[] deer =
        "{\"formatVersion\":\"0.5\",\"formatVersion\":\"0.5\"}".getBytes(StandardCharsets.UTF_8);

    InputSnapshot snapshot = new ProjectInputReader().read(createProject(deer));

    assertIssue(snapshot, IssueCode.JSON_DUPLICATE_KEY);
    assertTrue(snapshot.document().isEmpty());
  }

  @Test
  void rejectsDeerOneByteAboveTheContractLimit() throws IOException {
    int limit = ContractCapabilities.MAX_DEER_BYTES;
    byte[] deer = new byte[limit + 1];

    InputSnapshot snapshot = new ProjectInputReader().read(createProject(deer));

    assertIssue(snapshot, IssueCode.INPUT_DEER_TOO_LARGE);
    assertTrue(snapshot.rawDeerSha256().isEmpty());
    assertTrue(snapshot.document().isEmpty());
  }

  @Test
  void rejectsMissingProjectAndMissingDeerWithoutWriting() throws IOException {
    Path missing = temporaryDirectory.resolve("missing");
    InputSnapshot missingProject = new ProjectInputReader().read(missing);
    assertIssue(missingProject, IssueCode.INPUT_PROJECT_INVALID);
    assertFalse(Files.exists(missing));

    Path emptyProject = Files.createDirectory(temporaryDirectory.resolve("empty"));
    InputSnapshot missingDeer = new ProjectInputReader().read(emptyProject);
    assertIssue(missingDeer, IssueCode.INPUT_DEER_UNREADABLE);
    assertEquals(0L, entryCount(emptyProject));
  }

  private Path createProject(final byte[] deer) throws IOException {
    Path project = Files.createTempDirectory(temporaryDirectory, "project-");
    Files.write(project.resolve("deer.json"), deer);
    return project;
  }

  private static long entryCount(final Path directory) throws IOException {
    try (Stream<Path> entries = Files.list(directory)) {
      return entries.count();
    }
  }

  private static void assertIssue(final InputSnapshot snapshot, final IssueCode code) {
    assertTrue(snapshot.issues().stream().anyMatch(issue -> issue.code() == code));
  }

  private static String sha256(final byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }
}

package wizard.runner.asset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import wizard.runner.contract.ContractCapabilities;
import wizard.runner.contract.IssueCode;
import wizard.runner.contract.IssueCollector;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.model.ProjectDefinition;
import wizard.runner.model.ProjectDefinition.Asset;
import wizard.runner.validation.ProjectValidationPipeline;

/** Focused production-path tests for bounded, hostile-file asset verification. */
final class AssetVerifierTest {
  private static final String ORIGINAL_ASSET = "foundation-note-3b50ea522803.png";

  @TempDir Path temporaryDirectory;

  @Test
  void rejectsAFileAboveThePerAssetCeilingWithoutReadingItsContent() throws IOException {
    Fixture fixture = fixture("per-file");
    int limit = ContractCapabilities.MAX_ASSET_BYTES;
    Path oversized = fixture.customDirectory().resolve("oversized-000000000000.png");
    try (RandomAccessFile file = new RandomAccessFile(oversized.toFile(), "rw")) {
      file.setLength((long) limit + 1);
    }
    ProjectDefinition project = withSingleAssetPath(fixture.project(), oversized.getFileName());

    List<ValidationIssue> issues = verify(project, fixture.root());

    assertCapacity(issues, "assetBytes", (long) limit + 1);
  }

  @Test
  void rejectsCustomAssetsReachedThroughAnIntermediateDirectoryLink() throws IOException {
    Fixture fixture = fixture("linked-assets");
    byte[] original = Files.readAllBytes(fixture.customDirectory().resolve(ORIGINAL_ASSET));
    Files.delete(fixture.customDirectory().resolve(ORIGINAL_ASSET));
    Files.delete(fixture.customDirectory());
    Files.delete(fixture.root().resolve("assets"));
    Path externalAssets = Files.createDirectories(temporaryDirectory.resolve("external-assets"));
    Files.createDirectory(externalAssets.resolve("custom"));
    Files.write(externalAssets.resolve("custom").resolve(ORIGINAL_ASSET), original);
    try {
      Files.createSymbolicLink(fixture.root().resolve("assets"), externalAssets);
    } catch (IOException | UnsupportedOperationException | SecurityException exception) {
      Assumptions.assumeTrue(false, () -> "Directory links unavailable: " + exception.getMessage());
    }

    List<ValidationIssue> issues = verify(fixture.project(), fixture.root());

    assertTrue(issues.stream().anyMatch(issue -> issue.code() == IssueCode.ASSET_PATH_UNSAFE));
  }

  @Test
  void acceptsKnownBundledAssetsWithoutACustomDirectory() throws IOException {
    List<String> paths =
        List.of("items/puzzle-piece.png", "emotes/emote_cloud.png", "images/open-book.png");
    for (String path : paths) {
      Fixture fixture = fixture("bundled-" + path.substring(path.lastIndexOf('/') + 1));
      removeCustomDirectory(fixture);
      ProjectDefinition project = withSingleAsset(fixture.project(), path, "image/png");
      IssueCollector issues = new IssueCollector();

      List<?> verified = new AssetVerifier().verify(project, fixture.root(), issues);

      assertTrue(issues.issues().isEmpty(), issues.issues().toString());
      assertTrue(verified.isEmpty());
    }
  }

  @Test
  void mapsBundledLookupWithoutInferringMediaTypeFromThePath() throws IOException {
    Fixture missingFixture = fixture("bundled-missing");
    removeCustomDirectory(missingFixture);
    List<ValidationIssue> missing =
        verify(
            withSingleAsset(missingFixture.project(), "items/not-a-real-image.png", "image/png"),
            missingFixture.root());

    Fixture mismatchFixture = fixture("bundled-mismatch");
    removeCustomDirectory(mismatchFixture);
    List<ValidationIssue> mismatch =
        verify(
            withSingleAsset(mismatchFixture.project(), "items/puzzle-piece.png", "image/jpeg"),
            mismatchFixture.root());

    assertIssue(missing, IssueCode.ASSET_MISSING);
    assertTrue(mismatch.isEmpty(), mismatch.toString());
  }

  @Test
  void unionsEveryInternalAssetListCaseSensitively() throws IOException {
    Path dungeonList = temporaryDirectory.resolve("dungeon-assets.txt");
    Path escapeRoomList = temporaryDirectory.resolve("escape-room-assets.txt");
    Files.writeString(dungeonList, "items/puzzle-piece.png\n");
    Files.writeString(escapeRoomList, "images/open-book.png\n");

    Set<String> assets =
        AssetVerifier.readInternalAssets(
            Collections.enumeration(
                List.of(dungeonList.toUri().toURL(), escapeRoomList.toUri().toURL())));

    assertEquals(Set.of("items/puzzle-piece.png", "images/open-book.png"), assets);
    assertThrows(UnsupportedOperationException.class, () -> assets.add("items/other.png"));
  }

  @Test
  void rejectsMissingAndUnreadableInternalAssetLists() throws IOException {
    assertThrows(
        IllegalStateException.class,
        () -> AssetVerifier.readInternalAssets(Collections.emptyEnumeration()));
    URL unreadable =
        URL.of(
            URI.create("test://unreadable/internal_assets.txt"),
            new URLStreamHandler() {
              @Override
              protected URLConnection openConnection(final URL url) throws IOException {
                throw new IOException("unreadable test resource");
              }
            });

    assertThrows(
        IllegalStateException.class,
        () -> AssetVerifier.readInternalAssets(Collections.enumeration(List.of(unreadable))));
  }

  private Fixture fixture(final String name) throws IOException {
    Path examples = Path.of("examples", "foundation-v0.5").toAbsolutePath().normalize();
    Path root = Files.createDirectory(temporaryDirectory.resolve(name));
    Path custom = Files.createDirectories(root.resolve("assets/custom"));
    Files.copy(examples.resolve("deer.json"), root.resolve("deer.json"));
    Files.copy(
        examples.resolve("assets/custom").resolve(ORIGINAL_ASSET), custom.resolve(ORIGINAL_ASSET));
    ProjectDefinition project =
        new ProjectValidationPipeline().validate(root).model().orElseThrow();
    return new Fixture(root.toRealPath(), custom, project);
  }

  private static List<ValidationIssue> verify(final ProjectDefinition project, final Path root) {
    IssueCollector issues = new IssueCollector();
    new AssetVerifier().verify(project, root, issues);
    return issues.issues();
  }

  private static ProjectDefinition withSingleAssetPath(
      final ProjectDefinition project, final Path filename) {
    return withSingleAsset(
        project, "assets/custom/" + filename, project.assets().getFirst().mediaType());
  }

  private static ProjectDefinition withSingleAsset(
      final ProjectDefinition project, final String path, final String mediaType) {
    Asset original = project.assets().getFirst();
    Asset replacement = new Asset(original.id(), path, mediaType);
    return new ProjectDefinition(
        project.seed(),
        project.metadata(),
        project.session(),
        project.scenario(),
        project.surfaces(),
        project.riddleGraph(),
        project.riddles(),
        List.of(replacement));
  }

  private static void removeCustomDirectory(final Fixture fixture) throws IOException {
    Files.delete(fixture.customDirectory().resolve(ORIGINAL_ASSET));
    Files.delete(fixture.customDirectory());
    Files.delete(fixture.root().resolve("assets"));
  }

  private static void assertIssue(
      final List<ValidationIssue> issues, final IssueCode expectedCode) {
    assertTrue(issues.stream().anyMatch(issue -> issue.code() == expectedCode), issues.toString());
  }

  private static void assertCapacity(
      final List<ValidationIssue> issues, final String kind, final long actual) {
    ValidationIssue issue =
        issues.stream()
            .filter(candidate -> candidate.code() == IssueCode.RUNNER_CAPACITY_EXCEEDED)
            .filter(candidate -> kind.equals(candidate.arguments().get("dimension")))
            .findFirst()
            .orElseThrow();
    assertEquals(actual, ((Number) issue.arguments().get("actual")).longValue());
  }

  private record Fixture(Path root, Path customDirectory, ProjectDefinition project) {}
}

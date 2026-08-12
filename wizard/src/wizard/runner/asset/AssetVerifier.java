package wizard.runner.asset;

import foundation.room.model.VerifiedAsset;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import wizard.runner.asset.VerifiedImageReader.Failure;
import wizard.runner.asset.VerifiedImageReader.Reason;
import wizard.runner.contract.ContractCapabilities;
import wizard.runner.contract.IssueCode;
import wizard.runner.contract.IssueCollector;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.contract.ValidationIssue.Entity;
import wizard.runner.contract.ValidationPhase;
import wizard.runner.contract.ValidationSeverity;
import wizard.runner.model.ProjectDefinition;
import wizard.runner.model.ProjectDefinition.Asset;
import wizard.runner.model.ProjectDefinition.ResourceKind;

/** Verifies referenced host assets and maps shared read failures to Runner diagnostics. */
public final class AssetVerifier {
  private static final String CUSTOM_PREFIX = "assets/custom/";

  /** Creates a verifier using the published Runner limits. */
  public AssetVerifier() {}

  /**
   * Verifies every referenced asset and returns the bytes of custom images.
   *
   * @param project schema-valid authoring definition
   * @param realProjectRoot resolved project boundary
   * @param issues destination for deterministic diagnostics
   * @return path-sorted verified assets; callers discard them when errors exist
   */
  public List<VerifiedAsset> verify(
      final ProjectDefinition project, final Path realProjectRoot, final IssueCollector issues) {
    Objects.requireNonNull(project, "project");
    Path projectRoot =
        Objects.requireNonNull(realProjectRoot, "realProjectRoot").toAbsolutePath().normalize();
    Objects.requireNonNull(issues, "issues");

    Set<String> referenced = referencedIds(project);
    capacity(
        "referencedAssets",
        referenced.size(),
        ContractCapabilities.MAX_REFERENCED_ASSETS,
        "/assets",
        issues);
    validateAssetPaths(project.assets(), issues);

    Path customRoot = projectRoot.resolve("assets").resolve("custom").normalize();
    inspectDirectory(projectRoot, customRoot, referenced, project.assets(), issues);
    boolean customDirectorySafe = safeDirectory(projectRoot, customRoot);
    boolean hasReferencedCustom =
        project.assets().stream()
            .anyMatch(asset -> referenced.contains(asset.id()) && isCustom(asset.path()));
    if (hasReferencedCustom && !customDirectorySafe) {
      issues.add(
          issue(
              ValidationSeverity.ERROR,
              IssueCode.ASSET_PATH_UNSAFE,
              "validation.assets.directory_unsafe",
              Map.of(),
              "/assets",
              null));
    }

    boolean hasReferencedBundled =
        project.assets().stream()
            .anyMatch(asset -> referenced.contains(asset.id()) && !isCustom(asset.path()));
    Set<String> internalAssets = hasReferencedBundled ? internalAssets() : Set.of();
    List<VerifiedAsset> result = new ArrayList<>();
    long totalBytes = 0;
    for (int index = 0; index < project.assets().size(); index++) {
      Asset asset = project.assets().get(index);
      if (!referenced.contains(asset.id())) {
        continue;
      }
      if (!isCustom(asset.path())) {
        verifyBundled(asset, index, internalAssets, issues);
        continue;
      }
      if (!customDirectorySafe) {
        continue;
      }
      int assetIndex = index;
      long remainingAggregateBytes = ContractCapabilities.MAX_REFERENCED_ASSET_BYTES - totalBytes;
      try {
        VerifiedAsset verified =
            VerifiedImageReader.read(
                customRoot,
                asset.path(),
                asset.mediaType(),
                Math.min(ContractCapabilities.MAX_ASSET_BYTES, remainingAggregateBytes));
        String filename = VerifiedImageReader.filename(asset.path());
        int extensionIndex = filename.lastIndexOf('.');
        String expectedHashSuffix = "-" + verified.sha256().substring(0, 12);
        if (extensionIndex < expectedHashSuffix.length()
            || !filename.substring(0, extensionIndex).endsWith(expectedHashSuffix)) {
          assetIssue(
              asset,
              IssueCode.ASSET_HASH_MISMATCH,
              "validation.assets.hash_mismatch",
              "/assets/" + assetIndex + "/path",
              issues);
        }
        totalBytes += verified.bytes().length;
        result.add(verified);
      } catch (Failure failure) {
        mapFailure(
            asset,
            assetIndex,
            failure,
            remainingAggregateBytes < ContractCapabilities.MAX_ASSET_BYTES,
            issues);
        if (failure.reason() == Reason.TOO_LARGE
            && totalBytes > 0
            && ContractCapabilities.MAX_REFERENCED_ASSET_BYTES - totalBytes
                < ContractCapabilities.MAX_ASSET_BYTES) {
          break;
        }
      }
    }
    result.sort(Comparator.comparing(VerifiedAsset::logicalPath));
    return List.copyOf(result);
  }

  private static void verifyBundled(
      final Asset asset,
      final int index,
      final Set<String> internalAssets,
      final IssueCollector issues) {
    String path = "/assets/" + index + "/path";
    if (!internalAssets.contains(asset.path())) {
      assetIssue(asset, IssueCode.ASSET_MISSING, "validation.assets.missing", path, issues);
    }
  }

  private static Set<String> internalAssets() {
    ClassLoader classLoader = AssetVerifier.class.getClassLoader();
    if (classLoader == null) {
      throw new IllegalStateException("AssetVerifier class loader is unavailable");
    }
    try {
      return readInternalAssets(classLoader.getResources("internal_assets.txt"));
    } catch (IOException | SecurityException exception) {
      throw new IllegalStateException("Packaged internal_assets.txt cannot be read", exception);
    }
  }

  static Set<String> readInternalAssets(final Enumeration<URL> resources) {
    Set<String> result = new HashSet<>();
    boolean found = false;
    try {
      while (resources.hasMoreElements()) {
        found = true;
        URL resource = resources.nextElement();
        try (InputStream input = resource.openStream();
            BufferedReader reader =
                new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
          String line;
          while ((line = reader.readLine()) != null) {
            result.add(line);
          }
        }
      }
    } catch (IOException | SecurityException exception) {
      throw new IllegalStateException("Packaged internal_assets.txt cannot be read", exception);
    }
    if (!found) {
      throw new IllegalStateException("Packaged internal_assets.txt is missing");
    }
    return Set.copyOf(result);
  }

  private static void mapFailure(
      final Asset asset,
      final int index,
      final Failure failure,
      final boolean aggregateExhausted,
      final IssueCollector issues) {
    String path = "/assets/" + index + "/path";
    switch (failure.reason()) {
      case UNSAFE_PATH ->
          assetIssue(
              asset, IssueCode.ASSET_PATH_UNSAFE, "validation.assets.path_unsafe", path, issues);
      case MISSING ->
          assetIssue(asset, IssueCode.ASSET_MISSING, "validation.assets.missing", path, issues);
      case TYPE_MISMATCH ->
          assetIssue(
              asset,
              IssueCode.ASSET_CONTENT_MISMATCH,
              "validation.assets.content_mismatch",
              "/assets/" + index + "/mediaType",
              issues);
      case TOO_LARGE ->
          capacity(
              aggregateExhausted ? "referencedAssetBytes" : "assetBytes",
              aggregateExhausted
                  ? ContractCapabilities.MAX_REFERENCED_ASSET_BYTES + 1L
                  : ContractCapabilities.MAX_ASSET_BYTES + 1L,
              aggregateExhausted
                  ? ContractCapabilities.MAX_REFERENCED_ASSET_BYTES
                  : ContractCapabilities.MAX_ASSET_BYTES,
              aggregateExhausted ? "/assets" : path,
              issues);
      case IMAGE_CAPACITY ->
          issues.add(
              issue(
                  ValidationSeverity.ERROR,
                  IssueCode.RUNNER_CAPACITY_EXCEEDED,
                  "validation.assets.image_capacity_exceeded",
                  Map.of(
                      "height",
                      failure.imageHeight(),
                      "maxDimension",
                      ContractCapabilities.MAX_IMAGE_DIMENSION,
                      "maxPixels",
                      ContractCapabilities.MAX_IMAGE_PIXELS,
                      "width",
                      failure.imageWidth()),
                  path,
                  asset));
    }
  }

  private static void inspectDirectory(
      final Path projectRoot,
      final Path customRoot,
      final Set<String> referenced,
      final List<Asset> assets,
      final IssueCollector issues) {
    if (!Files.exists(customRoot, LinkOption.NOFOLLOW_LINKS)
        || !safeDirectory(projectRoot, customRoot)) {
      return;
    }
    Set<String> referencedPaths = new HashSet<>();
    for (Asset asset : assets) {
      if (referenced.contains(asset.id()) && isCustom(asset.path())) {
        referencedPaths.add(asset.path());
      }
    }
    try (DirectoryStream<Path> entries = Files.newDirectoryStream(customRoot)) {
      int entryLimit = ContractCapabilities.MAX_ASSET_DIRECTORY_ENTRIES;
      List<String> names = new ArrayList<>(Math.min(entryLimit, 128));
      for (Path entry : entries) {
        names.add(entry.getFileName().toString());
        if (names.size() > entryLimit) {
          capacity("assetDirectoryEntries", names.size(), entryLimit, "/assets", issues);
          return;
        }
      }
      names.sort(Comparator.naturalOrder());
      for (String name : names) {
        String path = "assets/custom/" + name;
        if (!referencedPaths.contains(path)) {
          issues.add(
              issue(
                  ValidationSeverity.WARNING,
                  IssueCode.ASSET_FILE_UNREFERENCED,
                  "validation.assets.file_unreferenced",
                  Map.of("path", path),
                  "/assets",
                  null));
        }
      }
    } catch (IOException | SecurityException exception) {
      issues.add(
          issue(
              ValidationSeverity.ERROR,
              IssueCode.ASSET_PATH_UNSAFE,
              "validation.assets.directory_unreadable",
              Map.of(),
              "/assets",
              null));
    }
  }

  private static boolean safeDirectory(final Path projectRoot, final Path directory) {
    if (!directory.startsWith(projectRoot)) {
      return false;
    }
    try {
      Path current = projectRoot;
      for (Path component : projectRoot.relativize(directory)) {
        current = current.resolve(component);
        BasicFileAttributes attributes =
            Files.readAttributes(current, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (!attributes.isDirectory() || attributes.isSymbolicLink() || attributes.isOther()) {
          return false;
        }
      }
      return Files.isReadable(directory) && directory.toRealPath().startsWith(projectRoot);
    } catch (IOException | SecurityException exception) {
      return false;
    }
  }

  private static void validateAssetPaths(final List<Asset> assets, final IssueCollector issues) {
    Map<String, String> normalized = new HashMap<>();
    for (int index = 0; index < assets.size(); index++) {
      Asset asset = assets.get(index);
      String pointer = "/assets/" + index + "/path";
      if (isCustom(asset.path()) && !portableCustomPath(asset.path())) {
        assetIssue(
            asset, IssueCode.ASSET_PATH_UNSAFE, "validation.assets.path_unsafe", pointer, issues);
        continue;
      }
      String portable =
          isCustom(asset.path())
              ? Normalizer.normalize(asset.path(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT)
              : asset.path();
      String previous = normalized.putIfAbsent(portable, pointer);
      if (previous != null) {
        issues.add(
            new ValidationIssue(
                ValidationSeverity.ERROR,
                ValidationPhase.ASSETS,
                IssueCode.ASSET_PATH_DUPLICATE,
                "validation.assets.path_duplicate",
                Map.of("path", asset.path()),
                pointer,
                Optional.of(new Entity("asset", asset.id())),
                List.of(previous)));
      }
    }
  }

  private static boolean portableCustomPath(final String path) {
    try {
      VerifiedImageReader.filename(path);
      return true;
    } catch (Failure failure) {
      return false;
    }
  }

  private static boolean isCustom(final String path) {
    return path.startsWith(CUSTOM_PREFIX);
  }

  private static Set<String> referencedIds(final ProjectDefinition project) {
    return project.riddles().stream()
        .flatMap(riddle -> riddle.informationSources().stream())
        .flatMap(source -> source.resources().stream())
        .filter(resource -> resource.kind() == ResourceKind.ASSET)
        .flatMap(resource -> resource.assetId().stream())
        .collect(Collectors.toUnmodifiableSet());
  }

  private static void capacity(
      final String dimension,
      final long actual,
      final long limit,
      final String path,
      final IssueCollector issues) {
    if (actual <= limit) {
      return;
    }
    issues.add(
        issue(
            ValidationSeverity.ERROR,
            IssueCode.RUNNER_CAPACITY_EXCEEDED,
            "validation.runner.capacity_exceeded",
            Map.of("actual", actual, "dimension", dimension, "limit", limit),
            path,
            null));
  }

  private static void assetIssue(
      final Asset asset,
      final IssueCode code,
      final String message,
      final String path,
      final IssueCollector issues) {
    issues.add(issue(ValidationSeverity.ERROR, code, message, Map.of(), path, asset));
  }

  private static ValidationIssue issue(
      final ValidationSeverity severity,
      final IssueCode code,
      final String message,
      final Map<String, Object> arguments,
      final String path,
      final Asset asset) {
    return new ValidationIssue(
        severity,
        ValidationPhase.ASSETS,
        code,
        message,
        arguments,
        path,
        asset == null ? Optional.empty() : Optional.of(new Entity("asset", asset.id())),
        List.of());
  }
}

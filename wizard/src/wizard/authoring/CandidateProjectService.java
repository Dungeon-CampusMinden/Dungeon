package wizard.authoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import wizard.runner.ProjectValidationService;
import wizard.runner.WizardRoomPackager;
import wizard.runner.asset.VerifiedImageReader;
import wizard.runner.contract.ContractCapabilities;
import wizard.runner.report.ProjectValidationReport;
import wizard.runner.validation.ValidationResult;

/** Stages browser-owned candidates and commits validated projects without host-side state. */
final class CandidateProjectService {
  private static final int WINDOWS_REPARSE_POINT = 0x400;
  private static final boolean WINDOWS =
      System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("windows");

  ValidationResponse validate(final JsonNode request) {
    requireFields(request, Set.of("project", "customAssets"));
    PreparedCandidate prepared = prepare(request);
    try {
      return new ValidationResponse(validateProject(prepared.directory()).report());
    } finally {
      deleteTree(prepared.directory());
    }
  }

  ValidationResponse finalizeProject(final JsonNode request) {
    requireFields(request, Set.of("project", "customAssets", "projectDirectory"));
    Path target = projectDirectory(request);
    PreparedCandidate prepared = prepare(request);
    try {
      ProjectValidationService.Outcome outcome = validateProject(prepared.directory());
      if (!outcome.report().valid()) {
        return new ValidationResponse(outcome.report());
      }
      String projectId = outcome.validation().orElseThrow().model().orElseThrow().metadata().id();
      prepareTarget(target, projectId);
      if (!prepared.assets().isEmpty()) {
        ensureOutputDirectory(target.resolve("assets"));
        ensureOutputDirectory(target.resolve("assets/custom"));
      }
      for (PreparedAsset asset : prepared.assets()) {
        Path output = target.resolve(asset.relativePath()).normalize();
        requireSafeOutput(output, target);
        AtomicFiles.replace(output, asset.bytes());
      }
      requireSafeOutput(target.resolve("deer.json"), target);
      AtomicFiles.replace(target.resolve("deer.json"), prepared.deerBytes());
      return new ValidationResponse(outcome.report());
    } catch (IOException exception) {
      throw new IllegalStateException("Finalized project could not be committed", exception);
    } finally {
      deleteTree(prepared.directory());
    }
  }

  PackageResponse packageProject(final JsonNode request, final Path template) {
    requireFields(request, Set.of("projectDirectory", "projectId"));
    Path project = projectDirectory(request);
    String projectId = requiredText(request, "projectId");
    requireSafeExistingDirectory(project, "Project directory is not a regular directory");
    ProjectValidationService.Outcome outcome = validateProject(project);
    if (!outcome.report().valid()) {
      return new PackageResponse(outcome.report(), null);
    }
    ValidationResult validation = outcome.validation().orElseThrow();
    if (!validation.model().orElseThrow().metadata().id().equals(projectId)) {
      throw new IllegalArgumentException("projectId does not match project metadata.id");
    }
    Path output = project.resolve("WizardRoom.jar");
    requireSafeOutput(output, project);
    WizardRoomPackager.packageValidatedProject(template, project, output, validation);
    return new PackageResponse(outcome.report(), output.toAbsolutePath().normalize().toString());
  }

  private static ProjectValidationService.Outcome validateProject(final Path directory) {
    return new ProjectValidationService().validate(directory);
  }

  private static PreparedCandidate prepare(final JsonNode request) {
    JsonNode project = request.get("project");
    JsonNode customAssets = request.get("customAssets");
    if (project == null || !project.isObject() || customAssets == null || !customAssets.isArray()) {
      throw new IllegalArgumentException("Request must contain project and customAssets");
    }
    byte[] deerBytes = AuthoringJson.encode(project);
    if (deerBytes.length > ContractCapabilities.MAX_DEER_BYTES) {
      throw new IllegalArgumentException("project exceeds the DEER byte limit");
    }
    Map<String, byte[]> supplied = decodeAssets(customAssets);
    Set<String> expected = referencedCustomPaths(project);
    if (!supplied.keySet().equals(expected)) {
      throw new IllegalArgumentException(
          "customAssets must exactly match the referenced custom asset paths");
    }

    Path temporary = null;
    try {
      temporary = Files.createTempDirectory("dungeon-wizard-candidate-");
      Path customRoot = temporary.resolve("assets/custom");
      Files.createDirectories(customRoot);
      List<PreparedAsset> assets = new ArrayList<>();
      for (Map.Entry<String, byte[]> entry : supplied.entrySet()) {
        String filename;
        try {
          filename = VerifiedImageReader.filename(entry.getKey());
        } catch (VerifiedImageReader.Failure exception) {
          continue;
        }
        Path relative = Path.of("assets/custom").resolve(filename);
        AtomicFiles.replace(temporary.resolve(relative), entry.getValue());
        assets.add(new PreparedAsset(relative, entry.getValue()));
      }
      AtomicFiles.replace(temporary.resolve("deer.json"), deerBytes);
      return new PreparedCandidate(temporary, deerBytes, assets);
    } catch (IOException exception) {
      deleteTree(temporary);
      throw new IllegalStateException("Candidate staging storage failed", exception);
    }
  }

  private static Map<String, byte[]> decodeAssets(final JsonNode customAssets) {
    if (customAssets.size() > ContractCapabilities.MAX_REFERENCED_ASSETS) {
      throw new IllegalArgumentException("customAssets exceeds the asset count limit");
    }
    Map<String, byte[]> result = new LinkedHashMap<>();
    long total = 0;
    for (JsonNode asset : customAssets) {
      requireFields(asset, Set.of("path", "bytesBase64"));
      String path = requiredText(asset, "path");
      String encoded = requiredString(asset, "bytesBase64");
      if (encoded.length() % 4 != 0) {
        throw new IllegalArgumentException("Custom asset Base64 is not padded");
      }
      byte[] bytes;
      try {
        bytes = Base64.getDecoder().decode(encoded);
      } catch (IllegalArgumentException exception) {
        throw new IllegalArgumentException("Custom asset Base64 is invalid", exception);
      }
      if (!Base64.getEncoder().encodeToString(bytes).equals(encoded)) {
        throw new IllegalArgumentException("Custom asset Base64 is not canonical standard Base64");
      }
      if (bytes.length > ContractCapabilities.MAX_ASSET_BYTES) {
        throw new IllegalArgumentException("Custom asset exceeds the per-asset byte limit");
      }
      total += bytes.length;
      if (total > ContractCapabilities.MAX_REFERENCED_ASSET_BYTES) {
        throw new IllegalArgumentException("customAssets exceeds the aggregate byte limit");
      }
      if (result.putIfAbsent(path, bytes) != null) {
        throw new IllegalArgumentException("customAssets contains a duplicate path");
      }
    }
    return Map.copyOf(result);
  }

  private static Set<String> referencedCustomPaths(final JsonNode project) {
    Set<String> referencedIds = new HashSet<>();
    JsonNode riddles = project.path("riddles");
    if (riddles.isArray()) {
      for (JsonNode riddle : riddles) {
        for (JsonNode source : riddle.path("informationSources")) {
          for (JsonNode resource : source.path("resources")) {
            if ("asset".equals(resource.path("kind").stringValue())
                && resource.path("assetId").isString()) {
              referencedIds.add(resource.path("assetId").stringValue());
            }
          }
        }
      }
    }
    Set<String> result = new HashSet<>();
    JsonNode assets = project.path("assets");
    if (assets.isArray()) {
      for (JsonNode asset : assets) {
        if (asset.path("id").isString()
            && referencedIds.contains(asset.path("id").stringValue())
            && asset.path("path").isString()
            && asset.path("path").stringValue().startsWith("assets/custom/")) {
          result.add(asset.path("path").stringValue());
        }
      }
    }
    return Set.copyOf(result);
  }

  private static void prepareTarget(final Path target, final String projectId) throws IOException {
    try {
      requireSafeAncestors(target);
    } catch (IllegalArgumentException exception) {
      throw new ProjectDirectoryConflictException(exception);
    }
    if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
      Files.createDirectories(target);
      requireSafeAncestors(target);
      return;
    }
    try {
      requireSafeExistingDirectory(target, "Project directory is not a regular directory");
    } catch (IllegalArgumentException exception) {
      throw new ProjectDirectoryConflictException(exception);
    }
    if (directoryEmpty(target)) {
      return;
    }
    ProjectValidationService.Outcome existing = validateProject(target);
    boolean sameProject =
        existing.report().valid()
            && existing
                .validation()
                .orElseThrow()
                .model()
                .orElseThrow()
                .metadata()
                .id()
                .equals(projectId);
    if (!sameProject) {
      throw new ProjectDirectoryConflictException();
    }
  }

  private static void ensureOutputDirectory(final Path directory) throws IOException {
    requireSafeAncestors(directory);
    if (Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
      requireSafeExistingDirectory(directory, "Project output parent is not a regular directory");
    } else {
      Files.createDirectory(directory);
      requireSafeExistingDirectory(directory, "Project output parent is not a regular directory");
    }
  }

  private static void requireSafeOutput(final Path output, final Path root) {
    Path normalizedRoot = root.toAbsolutePath().normalize();
    Path normalizedOutput = output.toAbsolutePath().normalize();
    if (!normalizedOutput.startsWith(normalizedRoot)) {
      throw new IllegalArgumentException("Project output escapes the project directory");
    }
    requireSafeAncestors(normalizedOutput.getParent());
    if (Files.exists(normalizedOutput, LinkOption.NOFOLLOW_LINKS)
        && (linkedOrReparsePoint(normalizedOutput)
            || !Files.isRegularFile(normalizedOutput, LinkOption.NOFOLLOW_LINKS))) {
      throw new IllegalArgumentException("Project output is not a regular file");
    }
  }

  private static void requireSafeExistingDirectory(final Path path, final String message) {
    requireSafeAncestors(path);
    try {
      BasicFileAttributes attributes =
          Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      if (!attributes.isDirectory() || linkedOrReparsePoint(path, attributes)) {
        throw new IllegalArgumentException(message);
      }
    } catch (IOException exception) {
      throw new IllegalArgumentException(message, exception);
    }
  }

  private static void requireSafeAncestors(final Path path) {
    Path current = path.toAbsolutePath().normalize();
    while (current != null) {
      if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && linkedOrReparsePoint(current)) {
        throw new IllegalArgumentException("Project path contains a link or reparse point");
      }
      current = current.getParent();
    }
  }

  private static boolean linkedOrReparsePoint(final Path path) {
    try {
      return linkedOrReparsePoint(
          path, Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS));
    } catch (IOException exception) {
      throw new IllegalArgumentException("Project path cannot be inspected safely", exception);
    }
  }

  private static boolean linkedOrReparsePoint(final Path path, final BasicFileAttributes attributes)
      throws IOException {
    if (attributes.isSymbolicLink() || attributes.isOther()) {
      return true;
    }
    if (!WINDOWS) {
      return false;
    }
    Object raw = Files.getAttribute(path, "dos:attributes", LinkOption.NOFOLLOW_LINKS);
    if (!(raw instanceof Number number)) {
      throw new IOException("Windows reparse-point status is unavailable");
    }
    return (number.intValue() & WINDOWS_REPARSE_POINT) != 0;
  }

  private static boolean directoryEmpty(final Path directory) throws IOException {
    try (var entries = Files.newDirectoryStream(directory)) {
      return !entries.iterator().hasNext();
    }
  }

  private static Path projectDirectory(final JsonNode request) {
    try {
      return Path.of(requiredText(request, "projectDirectory")).toAbsolutePath().normalize();
    } catch (RuntimeException exception) {
      throw new IllegalArgumentException("projectDirectory is invalid", exception);
    }
  }

  private static String requiredText(final JsonNode node, final String field) {
    String value = requiredString(node, field);
    if (value.isBlank()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return value;
  }

  private static String requiredString(final JsonNode node, final String field) {
    JsonNode value = node.get(field);
    if (value == null || !value.isString()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return value.stringValue();
  }

  private static void requireFields(final JsonNode node, final Set<String> expected) {
    if (node == null || !node.isObject()) {
      throw new IllegalArgumentException("Request body must be a JSON object");
    }
    Set<String> actual = new HashSet<>();
    actual.addAll(node.propertyNames());
    if (!actual.equals(expected)) {
      throw new IllegalArgumentException("Request fields are invalid");
    }
  }

  private static void deleteTree(final Path root) {
    if (root == null || !Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
      return;
    }
    try (var paths = Files.walk(root)) {
      paths
          .sorted((left, right) -> right.compareTo(left))
          .forEach(
              path -> {
                try {
                  Files.deleteIfExists(path);
                } catch (IOException ignored) {
                  // The operating system can reclaim abandoned candidate files.
                }
              });
    } catch (IOException ignored) {
      // The operating system can reclaim an abandoned candidate directory.
    }
  }

  record ValidationResponse(ProjectValidationReport report) {
    Map<String, Object> json() {
      return Map.of("report", reportJson(report));
    }
  }

  record PackageResponse(ProjectValidationReport report, String jarPath) {
    Map<String, Object> json() {
      Map<String, Object> response = new LinkedHashMap<>();
      response.put("report", reportJson(report));
      response.put("jarPath", jarPath);
      return response;
    }
  }

  private static JsonNode reportJson(final ProjectValidationReport report) {
    return AuthoringJson.parse(
        report.canonicalJson().getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  private record PreparedCandidate(Path directory, byte[] deerBytes, List<PreparedAsset> assets) {
    PreparedCandidate {
      deerBytes = deerBytes.clone();
      assets = List.copyOf(assets);
    }

    @Override
    public byte[] deerBytes() {
      return deerBytes.clone();
    }
  }

  private record PreparedAsset(Path relativePath, byte[] bytes) {
    PreparedAsset {
      bytes = bytes.clone();
    }

    @Override
    public byte[] bytes() {
      return bytes.clone();
    }
  }

  static final class ProjectDirectoryConflictException extends IllegalArgumentException {
    ProjectDirectoryConflictException() {
      super("Project directory must be empty or contain a valid project with the same metadata.id");
    }

    ProjectDirectoryConflictException(final Throwable cause) {
      super(
          "Project directory must be empty or contain a valid project with the same metadata.id",
          cause);
    }
  }
}

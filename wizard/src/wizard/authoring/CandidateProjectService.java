package wizard.authoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import wizard.runner.ProjectValidationService;
import wizard.runner.WizardRoomPackager;
import wizard.runner.asset.VerifiedImageReader;
import wizard.runner.contract.ContractCapabilities;
import wizard.runner.report.ProjectValidationReport;

/** Stages browser-owned candidates and packages validated games without host-side state. */
final class CandidateProjectService {
  ValidationResponse validate(final JsonNode request) {
    requireFields(request, Set.of("project", "customAssets"));
    PreparedCandidate prepared = prepare(request);
    try {
      return new ValidationResponse(validateProject(prepared.directory()).report());
    } finally {
      deleteTree(prepared.directory());
    }
  }

  PackageResponse packageProject(final JsonNode request, final Path template) {
    requireFields(request, Set.of("project", "customAssets"));
    PreparedCandidate prepared = prepare(request);
    try {
      ProjectValidationService.Outcome outcome = validateProject(prepared.directory());
      if (!outcome.report().valid()) {
        return new PackageResponse(outcome.report(), null);
      }
      Path output = prepared.directory().resolve("WizardRoom.jar");
      WizardRoomPackager.packageValidatedProject(
          template, prepared.directory(), output, outcome.validation().orElseThrow());
      try {
        return new PackageResponse(outcome.report(), Files.readAllBytes(output));
      } catch (IOException exception) {
        throw new IllegalStateException("Wizard room JAR could not be read", exception);
      }
    } finally {
      deleteTree(prepared.directory());
    }
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
      if (!supplied.isEmpty()) {
        Files.createDirectories(temporary.resolve("assets/custom"));
      }
      for (Map.Entry<String, byte[]> entry : supplied.entrySet()) {
        String filename;
        try {
          filename = VerifiedImageReader.filename(entry.getKey());
        } catch (VerifiedImageReader.Failure exception) {
          continue;
        }
        Path relative = Path.of("assets/custom").resolve(filename);
        AtomicFiles.replace(temporary.resolve(relative), entry.getValue());
      }
      AtomicFiles.replace(temporary.resolve("deer.json"), deerBytes);
      return new PreparedCandidate(temporary);
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

  record PackageResponse(ProjectValidationReport report, byte[] jarBytes) {
    Map<String, Object> json() {
      return Map.of("report", reportJson(report));
    }
  }

  private static JsonNode reportJson(final ProjectValidationReport report) {
    return AuthoringJson.parse(
        report.canonicalJson().getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  private record PreparedCandidate(Path directory) {}
}

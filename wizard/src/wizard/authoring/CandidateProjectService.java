package wizard.authoring;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import wizard.runner.ProjectValidationService;
import wizard.runner.WizardRoomPackager;
import wizard.runner.asset.VerifiedImageReader;
import wizard.runner.report.ProjectValidationReport;
import wizard.runner.validation.ValidationResult;

/** Builds exact candidates, validates them in isolation, and commits finalized project bytes. */
final class CandidateProjectService {
  private static final long EPHEMERAL_SEED = 0L;
  private static final byte[] FINALIZED_PROJECT_IDENTITY_DOMAIN =
      "dungeon-wizard-finalized-project-v1".getBytes(StandardCharsets.UTF_8);
  private static final SecureRandom RANDOM = new SecureRandom();
  private final DraftStore drafts;

  CandidateProjectService(final DraftStore drafts) {
    this.drafts = drafts;
  }

  ValidationResponse validate(final String draftId, final JsonNode request) {
    synchronized (drafts) {
      long revision = drafts.requireCurrentRevision(draftId, request);
      String candidateHash = candidateHash(request);
      JsonNode draft =
          drafts.loadOptional(draftId).orElseThrow(() -> new MissingDraftException(draftId));
      long seed = finalizedSeed(draft).orElse(EPHEMERAL_SEED);
      PreparedCandidate prepared = prepare(draftId, request, seed);
      try {
        return new ValidationResponse(
            revision,
            candidateHash,
            new ProjectValidationService().validate(prepared.directory()).report());
      } finally {
        deleteTree(prepared.directory());
      }
    }
  }

  FinalizeResponse finalizeProject(final String draftId, final JsonNode request) {
    synchronized (drafts) {
      Path target = projectDirectory(request);
      Optional<Long> recoverySeed = recoverableSeed(draftId);
      long revision = drafts.requireCurrentRevision(draftId, request);
      String candidateHash = candidateHash(request);
      JsonNode draft =
          drafts.loadOptional(draftId).orElseThrow(() -> new MissingDraftException(draftId));
      long seed =
          finalizedSeed(draft).or(() -> recoverySeed).orElseGet(CandidateProjectService::newSeed);
      PreparedCandidate prepared = prepare(draftId, request, seed);
      try {
        ProjectValidationService.Outcome outcome =
            new ProjectValidationService().validate(prepared.directory());
        ProjectValidationReport report = outcome.report();
        if (!report.valid()) {
          return new FinalizeResponse(revision, candidateHash, report, null, null, null, null);
        }
        ValidationResult validation = outcome.validation().orElseThrow();
        drafts.requireProjectOwnership(draftId, target);
        String finalizedAt = Instant.now().toString();
        Receipt receipt =
            new Receipt(
                seed,
                target.toString(),
                finalizedAt,
                validation.rawDeerSha256().orElseThrow(),
                finalizedProjectSha256(validation));
        writeReceipt(draftId, receipt);
        ensureTarget(target);
        ensureOutputDirectory(target.resolve("assets"));
        ensureOutputDirectory(target.resolve("assets/custom"));

        for (PreparedAsset asset : prepared.assets()) {
          AtomicFiles.replace(target.resolve(asset.relativePath()), asset.bytes());
        }
        AtomicFiles.replace(target.resolve("deer.json"), prepared.deerBytes());
        long persistedRevision = persistFinalization(draftId, receipt);
        deleteReceipt(draftId);
        return new FinalizeResponse(
            persistedRevision,
            candidateHash,
            report,
            seed,
            finalizedAt,
            target.toString(),
            receipt.deerSha256());
      } catch (IOException exception) {
        throw new IllegalStateException("Finalized project could not be committed", exception);
      } finally {
        deleteTree(prepared.directory());
      }
    }
  }

  PackageResponse packageProject(
      final String draftId, final JsonNode request, final Path template) {
    synchronized (drafts) {
      long revision = drafts.requireCurrentRevision(draftId, request);
      JsonNode draft =
          drafts.loadOptional(draftId).orElseThrow(() -> new MissingDraftException(draftId));
      Receipt finalization = storedFinalization(draft);
      requireExpectedFinalization(request, finalization);
      requireNoPendingReceipt(draftId);
      Path project = Path.of(finalization.projectDirectory()).toAbsolutePath().normalize();
      if (!drafts.ownsProjectDirectory(draftId, project)) {
        throw new DraftStore.ProjectOwnershipConflictException(project, Set.of());
      }
      Path output = project.resolve("WizardRoom.jar");
      ProjectValidationService.Outcome outcome = new ProjectValidationService().validate(project);
      if (!outcome.report().valid()) {
        throw new IllegalArgumentException(outcome.report().canonicalJson());
      }
      ValidationResult validation = outcome.validation().orElseThrow();
      requireExactValidatedInput(validation, finalization);
      WizardRoomPackager.packageValidatedProject(template, project, output, validation);
      String jarPath = output.toAbsolutePath().normalize().toString();
      String jarSha256;
      try {
        jarSha256 = sha256(Files.readAllBytes(output));
        revision =
            drafts.persistPackage(draftId, finalizationNode(finalization), jarPath, jarSha256);
      } catch (IOException exception) {
        throw new IllegalStateException("Packaged JAR identity could not be stored", exception);
      }
      return new PackageResponse(
          revision,
          finalization.seed(),
          finalization.finalizedAt(),
          finalization.projectDirectory(),
          finalization.deerSha256(),
          jarPath,
          jarSha256);
    }
  }

  FinalizationStatus finalizationStatus(final String draftId) {
    synchronized (drafts) {
      JsonNode draft =
          drafts.loadOptional(draftId).orElseThrow(() -> new MissingDraftException(draftId));
      long revision = DraftStore.revision(draft);
      JsonNode value = draft.get("finalization");
      if (value == null || value.isNull()) {
        return FinalizationStatus.notFinalized(revision);
      }
      Receipt identity;
      try {
        identity = storedFinalization(draft);
      } catch (RuntimeException exception) {
        return FinalizationStatus.invalid(revision);
      }
      Path project = Path.of(identity.projectDirectory()).toAbsolutePath().normalize();
      boolean identityValid = false;
      if (drafts.ownsProjectDirectory(draftId, project)) {
        ProjectValidationService.Outcome outcome = new ProjectValidationService().validate(project);
        identityValid =
            outcome.report().valid()
                && exactValidatedInput(outcome.validation().orElseThrow(), identity);
      }
      if (!identityValid) {
        return FinalizationStatus.of(revision, "invalid", identity, null, null);
      }
      String jarPath = text(value, "jarPath");
      String jarSha256 = text(value, "jarSha256");
      Path expectedJar = project.resolve("WizardRoom.jar");
      Path storedJar = normalizedPath(jarPath);
      boolean jarReady =
          storedJar != null
              && jarSha256 != null
              && jarSha256.matches("[0-9a-f]{64}")
              && storedJar.equals(expectedJar)
              && regularFileHash(storedJar, jarSha256);
      return FinalizationStatus.of(
          revision,
          jarReady ? "ready" : "finalized",
          identity,
          jarReady ? jarPath : null,
          jarReady ? jarSha256 : null);
    }
  }

  void reconcileAll() {
    for (Map<String, Object> item : drafts.list()) {
      reconcile(item.get("draftId").toString());
    }
  }

  boolean reconcile(final String draftId) {
    synchronized (drafts) {
      return reconcileLocked(draftId);
    }
  }

  private boolean reconcileLocked(final String draftId) {
    Path receiptFile = receiptPath(draftId);
    if (!Files.exists(receiptFile, LinkOption.NOFOLLOW_LINKS)) {
      return true;
    }
    if (!Files.isRegularFile(receiptFile, LinkOption.NOFOLLOW_LINKS)) {
      return false;
    }
    try {
      Receipt receipt = readReceipt(draftId);
      drafts.requireProjectOwnership(draftId, Path.of(receipt.projectDirectory()));
      ProjectValidationService.Outcome outcome =
          new ProjectValidationService().validate(Path.of(receipt.projectDirectory()));
      boolean exactValidTarget =
          outcome.report().valid()
              && exactValidatedInput(outcome.validation().orElseThrow(), receipt);
      if (!exactValidTarget) {
        return false;
      }
      persistFinalization(draftId, receipt);
      deleteReceipt(draftId);
      return true;
    } catch (DraftStore.ProjectOwnershipConflictException
        | DraftStore.ProjectOwnershipUnavailableException exception) {
      throw exception;
    } catch (IOException | RuntimeException exception) {
      return false;
    }
  }

  private void requireNoPendingReceipt(final String draftId) {
    if (Files.exists(receiptPath(draftId), LinkOption.NOFOLLOW_LINKS)) {
      throw new IllegalStateException(
          "A pending finalization must be recovered before the project can be packaged");
    }
  }

  private Optional<Long> recoverableSeed(final String draftId) {
    if (!Files.exists(receiptPath(draftId), LinkOption.NOFOLLOW_LINKS)) {
      return Optional.empty();
    }
    if (reconcileLocked(draftId)) {
      JsonNode recovered =
          drafts.loadOptional(draftId).orElseThrow(() -> new MissingDraftException(draftId));
      throw new FinalizationRecoveredException(DraftStore.revision(recovered));
    }
    Receipt pending = readReceipt(draftId);
    return Optional.of(pending.seed());
  }

  private Receipt readReceipt(final String draftId) {
    try {
      JsonNode receipt = AuthoringJson.parse(Files.readAllBytes(receiptPath(draftId)));
      return new Receipt(
          DraftStore.requireSafeInteger(receipt.get("seed"), "Recovery receipt seed is invalid"),
          requiredText(
              receipt, "projectDirectory", "Recovery receipt project directory is invalid"),
          requiredText(receipt, "finalizedAt", "Recovery receipt time is invalid"),
          requiredHash(receipt, "deerSha256", "Recovery receipt deer hash is invalid"),
          requiredHash(
              receipt,
              "finalizedProjectSha256",
              "Recovery receipt finalized project hash is invalid"));
    } catch (IOException exception) {
      throw new IllegalStateException("Recovery receipt cannot be read", exception);
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException("Recovery receipt is invalid", exception);
    }
  }

  private PreparedCandidate prepare(final String draftId, final JsonNode request, final long seed) {
    if (!request.isObject()
        || !request.path("project").isObject()
        || !request.path("uploads").isObject()
        || request.path("project").has("seed")) {
      throw new IllegalArgumentException(
          "Candidate request must contain a seedless project and uploads");
    }
    ObjectNode candidate = (ObjectNode) request.path("project").deepCopy();
    candidate.put("seed", seed);
    byte[] deerBytes = AuthoringJson.encode(candidate);
    Map<String, String> uploads = uploadMap(request.path("uploads"));
    Path temporary = null;
    try {
      temporary = Files.createTempDirectory("dungeon-wizard-candidate-");
      Path custom = temporary.resolve("assets/custom");
      Files.createDirectories(custom);
      List<PreparedAsset> assets = materializeAssets(draftId, candidate, uploads, custom);
      AtomicFiles.replace(temporary.resolve("deer.json"), deerBytes);
      return new PreparedCandidate(temporary, deerBytes, assets);
    } catch (CandidateStorageException exception) {
      deleteTree(temporary);
      throw exception;
    } catch (IOException exception) {
      deleteTree(temporary);
      throw new IllegalStateException("Candidate staging storage failed", exception);
    } catch (RuntimeException exception) {
      deleteTree(temporary);
      throw new IllegalArgumentException("Candidate project could not be prepared", exception);
    }
  }

  private List<PreparedAsset> materializeAssets(
      final String draftId,
      final JsonNode candidate,
      final Map<String, String> uploads,
      final Path customRoot)
      throws IOException {
    List<PreparedAsset> result = new ArrayList<>();
    Set<String> usedUploadIds = new HashSet<>();
    Set<String> referencedAssetIds = referencedAssetIds(candidate);
    JsonNode assets = candidate.path("assets");
    if (!assets.isArray()) {
      return result;
    }
    for (JsonNode asset : assets) {
      String path = asset.path("path").stringValue();
      if (path == null || !path.startsWith("assets/custom/")) {
        continue;
      }
      String assetId = asset.path("id").stringValue();
      if (!referencedAssetIds.contains(assetId)) {
        continue;
      }
      String key = uploads.get(assetId);
      if (key == null) {
        continue;
      }
      usedUploadIds.add(assetId);
      Optional<DraftStore.Upload> storedUpload;
      try {
        storedUpload = drafts.upload(draftId, key);
      } catch (RuntimeException exception) {
        throw new CandidateStorageException("Stored upload cannot be read safely", exception);
      }
      if (storedUpload.isEmpty()) {
        continue;
      }
      DraftStore.Upload upload = storedUpload.orElseThrow();
      byte[] uploadBytes = upload.bytes();
      if (!sha256(uploadBytes).equals(key)) {
        throw new CandidateStorageException(
            "Stored upload bytes do not match their storage key", null);
      }
      String filename;
      try {
        filename = VerifiedImageReader.filename(path);
      } catch (VerifiedImageReader.Failure failure) {
        continue;
      }
      Path staged = customRoot.resolve(filename);
      AtomicFiles.replace(staged, uploadBytes);
      result.add(new PreparedAsset(Path.of("assets/custom").resolve(filename), uploadBytes));
    }
    if (!usedUploadIds.equals(uploads.keySet())) {
      throw new IllegalArgumentException(
          "Upload mappings must exactly match custom candidate assets");
    }
    return List.copyOf(result);
  }

  private static Set<String> referencedAssetIds(final JsonNode candidate) {
    Set<String> result = new HashSet<>();
    JsonNode riddles = candidate.path("riddles");
    if (!riddles.isArray()) {
      return Set.of();
    }
    for (JsonNode riddle : riddles) {
      for (JsonNode source : riddle.path("informationSources")) {
        for (JsonNode resource : source.path("resources")) {
          if ("asset".equals(resource.path("kind").stringValue())
              && resource.path("assetId").isString()) {
            result.add(resource.path("assetId").stringValue());
          }
        }
      }
    }
    return Set.copyOf(result);
  }

  private static Map<String, String> uploadMap(final JsonNode node) {
    Map<String, String> result = new HashMap<>();
    node.properties()
        .forEach(
            entry -> {
              if (!entry.getValue().isString()) {
                throw new IllegalArgumentException("Upload storage keys must be strings");
              }
              String key = entry.getValue().stringValue();
              DraftStore.requireStorageKey(key);
              result.put(entry.getKey(), key);
            });
    return Map.copyOf(result);
  }

  private static Path projectDirectory(final JsonNode request) {
    JsonNode value = request.get("projectDirectory");
    if (value == null || !value.isString() || value.stringValue().isBlank()) {
      throw new IllegalArgumentException("projectDirectory is required");
    }
    return Path.of(value.stringValue()).toAbsolutePath().normalize();
  }

  private static void ensureTarget(final Path target) throws IOException {
    if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
      if (Files.isSymbolicLink(target) || !Files.isDirectory(target, LinkOption.NOFOLLOW_LINKS)) {
        throw new IllegalArgumentException("Project directory is not a regular directory");
      }
    } else {
      Files.createDirectories(target);
    }
    if (!Files.isWritable(target)) {
      throw new IllegalArgumentException("Project directory is not writable");
    }
  }

  private static void ensureOutputDirectory(final Path directory) throws IOException {
    if (Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
      if (Files.isSymbolicLink(directory)
          || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
        throw new IllegalArgumentException("Project output parent is not a regular directory");
      }
      return;
    }
    Files.createDirectory(directory);
  }

  private Optional<Long> finalizedSeed(final JsonNode draft) {
    JsonNode seed = draft.path("finalization").get("seed");
    if (seed == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(
          DraftStore.requireSafeInteger(seed, "Stored finalization seed is invalid"));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }

  private long persistFinalization(final String draftId, final Receipt receipt) throws IOException {
    return drafts.persistFinalization(
        draftId,
        receipt.seed(),
        receipt.projectDirectory(),
        receipt.finalizedAt(),
        receipt.deerSha256(),
        receipt.finalizedProjectSha256());
  }

  private void deleteReceipt(final String draftId) {
    try {
      Files.deleteIfExists(receiptPath(draftId));
    } catch (IOException ignored) {
      // Recovery recognizes the already persisted identity without incrementing revision again.
    }
  }

  private static Receipt storedFinalization(final JsonNode draft) {
    JsonNode finalization = draft.get("finalization");
    if (finalization == null
        || !finalization.isObject()
        || !finalization.path("seed").isIntegralNumber()
        || !finalization.path("projectDirectory").isString()
        || finalization.path("projectDirectory").stringValue().isBlank()
        || !finalization.path("finalizedAt").isString()
        || finalization.path("finalizedAt").stringValue().isBlank()
        || !finalization.path("deerSha256").isString()
        || !finalization.path("deerSha256").stringValue().matches("[0-9a-f]{64}")
        || !finalization.path("finalizedProjectSha256").isString()
        || !finalization.path("finalizedProjectSha256").stringValue().matches("[0-9a-f]{64}")) {
      throw new IllegalStateException(
          "Draft has no packageable host-confirmed finalization; finalize it again");
    }
    long seed;
    try {
      seed =
          DraftStore.requireSafeInteger(
              finalization.get("seed"), "Stored finalization seed is invalid");
    } catch (IllegalArgumentException exception) {
      throw new IllegalStateException(exception.getMessage(), exception);
    }
    return new Receipt(
        seed,
        finalization.path("projectDirectory").stringValue(),
        finalization.path("finalizedAt").stringValue(),
        finalization.path("deerSha256").stringValue(),
        finalization.path("finalizedProjectSha256").stringValue());
  }

  private void writeReceipt(final String draftId, final Receipt receipt) throws IOException {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("seed", receipt.seed());
    body.put("projectDirectory", receipt.projectDirectory());
    body.put("finalizedAt", receipt.finalizedAt());
    body.put("deerSha256", receipt.deerSha256());
    body.put("finalizedProjectSha256", receipt.finalizedProjectSha256());
    AtomicFiles.replace(receiptPath(draftId), AuthoringJson.encode(body));
  }

  private Path receiptPath(final String draftId) {
    return drafts.draftDirectory(draftId).resolve("finalize-receipt.json");
  }

  private static long newSeed() {
    return RANDOM.nextLong() & DraftStore.MAX_SAFE_INTEGER;
  }

  private static String candidateHash(final JsonNode request) {
    String value = text(request, "candidateHash");
    if (value == null || !value.matches("[0-9a-f]{64}")) {
      throw new IllegalArgumentException("candidateHash must be a lowercase SHA-256 value");
    }
    return value;
  }

  private static void requireExpectedFinalization(
      final JsonNode request, final Receipt finalization) {
    long requestedSeed;
    try {
      requestedSeed = DraftStore.requireSafeInteger(request.get("seed"), "Package seed is invalid");
    } catch (IllegalArgumentException exception) {
      throw new DraftStore.FinalizationIdentityConflictException();
    }
    if (!request.isObject()
        || requestedSeed != finalization.seed()
        || !finalization.finalizedAt().equals(text(request, "finalizedAt"))
        || !finalization.projectDirectory().equals(text(request, "projectDirectory"))
        || !finalization.deerSha256().equals(text(request, "deerSha256"))) {
      throw new DraftStore.FinalizationIdentityConflictException();
    }
  }

  private static String text(final JsonNode node, final String field) {
    JsonNode value = node.get(field);
    return value != null && value.isString() ? value.stringValue() : null;
  }

  private static String requiredText(
      final JsonNode node, final String field, final String errorMessage) {
    String value = text(node, field);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(errorMessage);
    }
    return value;
  }

  private static String requiredHash(
      final JsonNode node, final String field, final String errorMessage) {
    String value = requiredText(node, field, errorMessage);
    if (!value.matches("[0-9a-f]{64}")) {
      throw new IllegalStateException(errorMessage);
    }
    return value;
  }

  private static Path normalizedPath(final String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Path.of(value).toAbsolutePath().normalize();
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private static ObjectNode finalizationNode(final Receipt identity) {
    ObjectNode node = AuthoringJson.MAPPER.createObjectNode();
    node.put("seed", identity.seed());
    node.put("finalizedAt", identity.finalizedAt());
    node.put("projectDirectory", identity.projectDirectory());
    node.put("deerSha256", identity.deerSha256());
    node.put("finalizedProjectSha256", identity.finalizedProjectSha256());
    return node;
  }

  private static void requireExactValidatedInput(
      final ValidationResult validation, final Receipt identity) {
    if (!exactValidatedInput(validation, identity)) {
      throw new IllegalStateException(
          "Finalized project inputs no longer match this draft's saved finalization");
    }
  }

  private static boolean exactValidatedInput(
      final ValidationResult validation, final Receipt identity) {
    return validation.rawDeerSha256().filter(identity.deerSha256()::equals).isPresent()
        && finalizedProjectSha256(validation).equals(identity.finalizedProjectSha256());
  }

  private static String finalizedProjectSha256(final ValidationResult validation) {
    MessageDigest digest = sha256Digest();
    updateLengthDelimited(digest, FINALIZED_PROJECT_IDENTITY_DOMAIN);
    updateLengthDelimited(
        digest, HexFormat.of().parseHex(validation.hostInputSha256().orElseThrow()));
    digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(validation.assets().size()).array());
    validation.assets().stream()
        .sorted(java.util.Comparator.comparing(asset -> asset.logicalPath()))
        .forEach(
            asset -> {
              updateLengthDelimited(digest, asset.logicalPath().getBytes(StandardCharsets.UTF_8));
              updateLengthDelimited(digest, HexFormat.of().parseHex(asset.sha256()));
            });
    return HexFormat.of().formatHex(digest.digest());
  }

  private static void updateLengthDelimited(final MessageDigest digest, final byte[] value) {
    digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(value.length).array());
    digest.update(value);
  }

  private static boolean regularFileHash(final Path file, final String expectedHash) {
    try {
      return Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)
          && sha256(Files.readAllBytes(file)).equals(expectedHash);
    } catch (IOException | RuntimeException exception) {
      return false;
    }
  }

  private static String sha256(final byte[] bytes) {
    return HexFormat.of().formatHex(sha256Digest().digest(bytes));
  }

  private static MessageDigest sha256Digest() {
    try {
      return MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
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
                  // Disposable candidate cleanup is best effort.
                }
              });
    } catch (IOException ignored) {
      // Disposable candidate cleanup is best effort.
    }
  }

  record ValidationResponse(long revision, String candidateHash, ProjectValidationReport report) {
    Map<String, Object> json() {
      return Map.of(
          "revision",
          revision,
          "candidateHash",
          candidateHash,
          "report",
          AuthoringJson.parse(
              report.canonicalJson().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
  }

  record FinalizeResponse(
      long revision,
      String candidateHash,
      ProjectValidationReport report,
      Long seed,
      String finalizedAt,
      String projectDirectory,
      String deerSha256) {
    Map<String, Object> json() {
      Map<String, Object> value = new LinkedHashMap<>();
      value.put("revision", revision);
      value.put("candidateHash", candidateHash);
      value.put(
          "report",
          AuthoringJson.parse(
              report.canonicalJson().getBytes(java.nio.charset.StandardCharsets.UTF_8)));
      value.put("seed", seed);
      value.put("finalizedAt", finalizedAt);
      value.put("projectDirectory", projectDirectory);
      value.put("deerSha256", deerSha256);
      return value;
    }
  }

  record PackageResponse(
      long revision,
      long seed,
      String finalizedAt,
      String projectDirectory,
      String deerSha256,
      String jarPath,
      String jarSha256) {}

  record FinalizationStatus(
      long revision,
      String status,
      Long seed,
      String finalizedAt,
      String projectDirectory,
      String deerSha256,
      String jarPath,
      String jarSha256) {
    static FinalizationStatus notFinalized(final long revision) {
      return new FinalizationStatus(revision, "not-finalized", null, null, null, null, null, null);
    }

    static FinalizationStatus invalid(final long revision) {
      return new FinalizationStatus(revision, "invalid", null, null, null, null, null, null);
    }

    static FinalizationStatus of(
        final long revision,
        final String status,
        final Receipt identity,
        final String jarPath,
        final String jarSha256) {
      return new FinalizationStatus(
          revision,
          status,
          identity.seed(),
          identity.finalizedAt(),
          identity.projectDirectory(),
          identity.deerSha256(),
          jarPath,
          jarSha256);
    }
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

  private record Receipt(
      long seed,
      String projectDirectory,
      String finalizedAt,
      String deerSha256,
      String finalizedProjectSha256) {}

  static final class MissingDraftException extends IllegalArgumentException {
    MissingDraftException(final String draftId) {
      super("Draft does not exist: " + draftId);
    }
  }

  static final class FinalizationRecoveredException extends IllegalStateException {
    private final long revision;

    FinalizationRecoveredException(final long revision) {
      super("A completed finalization was recovered; reload the draft before continuing");
      this.revision = revision;
    }

    long revision() {
      return revision;
    }
  }

  private static final class CandidateStorageException extends IllegalStateException {
    CandidateStorageException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }
}

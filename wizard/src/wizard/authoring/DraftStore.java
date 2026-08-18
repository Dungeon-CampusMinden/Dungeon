package wizard.authoring;

import escaperoom.foundation.room.model.VerifiedAsset;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import wizard.runner.asset.VerifiedImageReader;
import wizard.runner.contract.ContractCapabilities;

/** Private native draft and upload persistence rooted below one application-owned directory. */
final class DraftStore {
  static final String DATA_ROOT_PROPERTY = "dungeon.wizard.dataRoot";
  private static final Pattern DRAFT_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]{0,63}");
  private static final Pattern STORAGE_KEY = Pattern.compile("[0-9a-f]{64}");
  static final long MAX_SAFE_INTEGER = 9_007_199_254_740_991L;
  private static final int MAX_DRAFT_BYTES = ContractCapabilities.MAX_DEER_BYTES * 4;
  private final Path draftsRoot;

  DraftStore(final Path dataRoot) {
    draftsRoot = dataRoot.toAbsolutePath().normalize().resolve("drafts");
    try {
      Files.createDirectories(draftsRoot);
    } catch (IOException exception) {
      throw new IllegalStateException("Draft storage cannot be created", exception);
    }
  }

  static Path defaultDataRoot() {
    String override = System.getProperty(DATA_ROOT_PROPERTY);
    if (override != null && !override.isBlank()) {
      return Path.of(override);
    }
    String localAppData = System.getenv("LOCALAPPDATA");
    if (localAppData == null || localAppData.isBlank()) {
      throw new IllegalStateException("LOCALAPPDATA is unavailable");
    }
    return Path.of(localAppData).resolve("Dungeon Wizard");
  }

  synchronized List<Map<String, Object>> list() {
    List<Map<String, Object>> result = new ArrayList<>();
    try (var entries = Files.newDirectoryStream(draftsRoot)) {
      for (Path entry : entries) {
        if (!Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)
            || !DRAFT_ID.matcher(entry.getFileName().toString()).matches()) {
          continue;
        }
        Optional<JsonNode> draft;
        try {
          draft = loadOptional(entry.getFileName().toString());
        } catch (RuntimeException exception) {
          continue;
        }
        if (draft.isEmpty()) {
          continue;
        }
        JsonNode value = draft.orElseThrow();
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("draftId", entry.getFileName().toString());
        JsonNode title = value.path("project").path("metadata").get("title");
        item.put("title", title != null && title.isString() ? title.stringValue() : "");
        JsonNode savedAt = value.get("savedAt");
        if (savedAt != null && savedAt.isString()) {
          item.put("savedAt", savedAt.stringValue());
        }
        result.add(item);
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Draft list cannot be read", exception);
    }
    result.sort(Comparator.comparing(item -> item.get("draftId").toString()));
    return List.copyOf(result);
  }

  synchronized Optional<JsonNode> loadOptional(final String draftId) {
    Path file = draftDirectory(draftId).resolve("draft.json");
    if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
      return Optional.empty();
    }
    JsonNode stored = AuthoringJson.parse(readBounded(file, MAX_DRAFT_BYTES));
    if (!(stored instanceof ObjectNode draft)) {
      throw new IllegalStateException("Stored draft root is invalid");
    }
    JsonNode revision = draft.get("revision");
    if (revision == null) {
      draft.put("revision", 0L);
    } else {
      requireRevision(revision, "Stored draft revision is invalid");
    }
    normalizeJarFields(draft.get("finalization"));
    return Optional.of(draft);
  }

  private static void normalizeJarFields(final JsonNode finalization) {
    if (!(finalization instanceof ObjectNode value)) {
      return;
    }
    JsonNode jarPath = value.get("jarPath");
    JsonNode jarSha256 = value.get("jarSha256");
    boolean validPair =
        jarPath != null
            && jarPath.isString()
            && !jarPath.stringValue().isBlank()
            && jarSha256 != null
            && jarSha256.isString()
            && jarSha256.stringValue().matches("[0-9a-f]{64}");
    if (!validPair) {
      value.remove(List.of("jarPath", "jarSha256"));
    }
  }

  synchronized JsonNode save(final String draftId, final byte[] body) {
    if (body.length > MAX_DRAFT_BYTES) {
      throw new IllegalArgumentException("Draft snapshot is too large");
    }
    JsonNode parsed = AuthoringJson.parse(body);
    if (!(parsed instanceof ObjectNode draft)
        || !draftId.equals(text(draft, "draftId"))
        || !"1".equals(text(draft, "draftVersion"))
        || !draft.path("project").isObject()
        || !"0.4".equals(text(draft.path("project"), "formatVersion"))
        || draft.path("project").has("seed")) {
      throw new IllegalArgumentException("Draft routing, version, or project boundary is invalid");
    }
    long actualRevision = requireRevision(draft.get("revision"), "Draft revision is invalid");
    Optional<JsonNode> stored = loadOptional(draftId);
    long expectedRevision = stored.map(DraftStore::revision).orElse(0L);
    if (actualRevision != expectedRevision) {
      throw new RevisionConflictException(expectedRevision, actualRevision);
    }
    if (expectedRevision == MAX_SAFE_INTEGER) {
      throw new IllegalStateException("Draft revision cannot be incremented safely");
    }
    String savedAt = Instant.now().toString();
    mergeFinalization(draft, stored);
    draft.put("revision", expectedRevision + 1L);
    draft.put("savedAt", savedAt);
    draft.put("saveStatus", "saved");
    try {
      AtomicFiles.replace(
          draftDirectory(draftId).resolve("draft.json"), AuthoringJson.encode(draft));
      return draft.deepCopy();
    } catch (IOException exception) {
      throw new IllegalStateException("Draft snapshot cannot be stored", exception);
    }
  }

  Upload putUpload(
      final String draftId, final String originalName, final String mediaType, final byte[] bytes) {
    if (bytes.length == 0 || bytes.length > ContractCapabilities.MAX_ASSET_BYTES) {
      throw new IllegalArgumentException("Upload size is invalid");
    }
    String key = sha256(bytes);
    String extension = mediaType.equals("image/png") ? ".png" : ".jpg";
    Path uploadRoot = draftDirectory(draftId).resolve("uploads");
    Path upload = uploadRoot.resolve(key + extension);
    boolean existedBefore = Files.exists(upload, LinkOption.NOFOLLOW_LINKS);
    try {
      AtomicFiles.replace(upload, bytes);
      VerifiedAsset verified =
          VerifiedImageReader.read(
              uploadRoot,
              "assets/custom/" + key + extension,
              mediaType,
              ContractCapabilities.MAX_ASSET_BYTES);
      if (!verified.sha256().equals(key)) {
        throw new IllegalArgumentException("Upload hash changed while storing");
      }
      return new Upload(key, originalName, mediaType, bytes.clone());
    } catch (RuntimeException | IOException exception) {
      if (!existedBefore) {
        try {
          Files.deleteIfExists(upload);
        } catch (IOException ignored) {
          // The incomplete upload remains unreachable unless its full verified hash was returned.
        }
      }
      throw new IllegalArgumentException(
          "Upload is not a valid " + mediaType + " image", exception);
    }
  }

  List<String> uploadKeys(final String draftId) {
    Path root = draftDirectory(draftId).resolve("uploads");
    if (!Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
      return List.of();
    }
    List<String> keys = new ArrayList<>();
    try (var entries = Files.newDirectoryStream(root)) {
      for (Path entry : entries) {
        String name = entry.getFileName().toString();
        int dot = name.indexOf('.');
        if (Files.isRegularFile(entry, LinkOption.NOFOLLOW_LINKS)
            && dot == 64
            && STORAGE_KEY.matcher(name.substring(0, dot)).matches()
            && (name.endsWith(".png") || name.endsWith(".jpg"))) {
          keys.add(name.substring(0, dot));
        }
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Upload list cannot be read", exception);
    }
    keys.sort(Comparator.naturalOrder());
    return List.copyOf(keys);
  }

  Optional<Upload> upload(final String draftId, final String storageKey) {
    requireStorageKey(storageKey);
    Path root = draftDirectory(draftId).resolve("uploads");
    for (String extension : List.of(".png", ".jpg")) {
      Path candidate = root.resolve(storageKey + extension);
      if (Files.isRegularFile(candidate, LinkOption.NOFOLLOW_LINKS)) {
        String type = extension.equals(".png") ? "image/png" : "image/jpeg";
        return Optional.of(
            new Upload(
                storageKey,
                storageKey + extension,
                type,
                readBounded(candidate, ContractCapabilities.MAX_ASSET_BYTES)));
      }
    }
    return Optional.empty();
  }

  Path draftDirectory(final String draftId) {
    requireDraftId(draftId);
    return draftsRoot.resolve(draftId);
  }

  synchronized void requireProjectOwnership(final String draftId, final Path projectDirectory) {
    requireDraftId(draftId);
    Path target = projectDirectory.toAbsolutePath().normalize();
    Set<String> owners = projectOwners(target);
    if (owners.stream().anyMatch(owner -> !owner.equals(draftId))) {
      throw new ProjectOwnershipConflictException(target, owners);
    }
    if (owners.isEmpty() && directoryHasEntries(target)) {
      throw new ProjectOwnershipConflictException(target, Set.of());
    }
  }

  synchronized boolean ownsProjectDirectory(final String draftId, final Path projectDirectory) {
    Set<String> owners = projectOwners(projectDirectory.toAbsolutePath().normalize());
    return owners.size() == 1 && owners.contains(draftId);
  }

  private Set<String> projectOwners(final Path target) {
    Set<String> owners = new java.util.HashSet<>();
    try (var entries = Files.newDirectoryStream(draftsRoot)) {
      for (Path entry : entries) {
        String owner = entry.getFileName().toString();
        if (!Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)
            || !DRAFT_ID.matcher(owner).matches()) {
          continue;
        }
        addOwner(owners, owner, entry.resolve("draft.json"), "finalization", target);
        addOwner(owners, owner, entry.resolve("finalize-receipt.json"), null, target);
      }
    } catch (IOException exception) {
      throw new ProjectOwnershipUnavailableException(
          "Project ownership registry cannot be read", exception);
    }
    return Set.copyOf(owners);
  }

  private static void addOwner(
      final java.util.Set<String> owners,
      final String draftId,
      final Path file,
      final String nestedField,
      final Path target) {
    if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
      return;
    }
    JsonNode root;
    try {
      root = AuthoringJson.parse(readBounded(file, MAX_DRAFT_BYTES));
    } catch (RuntimeException exception) {
      throw new ProjectOwnershipUnavailableException(
          "Project ownership cannot be determined because draft " + draftId + " is corrupted",
          exception);
    }
    JsonNode identity = nestedField == null ? root : root.get(nestedField);
    if (identity == null || identity.isNull()) {
      return;
    }
    JsonNode directory = identity.get("projectDirectory");
    if (directory == null || !directory.isString() || directory.stringValue().isBlank()) {
      throw new ProjectOwnershipUnavailableException(
          "Project ownership cannot be determined because draft "
              + draftId
              + " has a corrupted finalization");
    }
    try {
      if (Path.of(directory.stringValue()).toAbsolutePath().normalize().equals(target)) {
        owners.add(draftId);
      }
    } catch (RuntimeException exception) {
      throw new ProjectOwnershipUnavailableException(
          "Project ownership cannot be determined because draft "
              + draftId
              + " has an invalid project directory",
          exception);
    }
  }

  private static boolean directoryHasEntries(final Path directory) {
    if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
      return false;
    }
    if (Files.isSymbolicLink(directory)
        || !Files.isDirectory(directory, LinkOption.NOFOLLOW_LINKS)) {
      return true;
    }
    try (var entries = Files.newDirectoryStream(directory)) {
      return entries.iterator().hasNext();
    } catch (IOException exception) {
      throw new IllegalStateException("Project directory cannot be inspected", exception);
    }
  }

  synchronized long persistFinalization(
      final String draftId,
      final long seed,
      final String projectDirectory,
      final String finalizedAt,
      final String deerSha256,
      final String finalizedProjectSha256)
      throws IOException {
    JsonNode stored =
        loadOptional(draftId).orElseThrow(() -> new IllegalArgumentException("Draft is missing"));
    if (!(stored instanceof ObjectNode draft)) {
      throw new IllegalArgumentException("Stored draft root is invalid");
    }
    ObjectNode finalization = AuthoringJson.MAPPER.createObjectNode();
    finalization.put("seed", seed);
    finalization.put("projectDirectory", projectDirectory);
    finalization.put("finalizedAt", finalizedAt);
    finalization.put("deerSha256", deerSha256);
    finalization.put("finalizedProjectSha256", finalizedProjectSha256);
    JsonNode previous = draft.get("finalization");
    boolean alreadyPersisted =
        previous instanceof ObjectNode previousFinalization
            && sameHostFinalization(finalization, previousFinalization)
            && equalText(finalization, previousFinalization, "deerSha256")
            && equalText(finalization, previousFinalization, "finalizedProjectSha256");
    if (alreadyPersisted) {
      return revision(draft);
    }
    if (previous instanceof ObjectNode previousFinalization
        && sameHostFinalization(finalization, previousFinalization)) {
      mergeUiField(finalization, previousFinalization, "candidateHash");
      mergeHostField(finalization, previousFinalization, "jarPath");
      mergeHostField(finalization, previousFinalization, "jarSha256");
    }
    draft.set("finalization", finalization);
    draft.put("savedAt", Instant.now().toString());
    draft.put("saveStatus", "saved");
    long revision = incrementRevision(draft);
    AtomicFiles.replace(draftDirectory(draftId).resolve("draft.json"), AuthoringJson.encode(draft));
    return revision;
  }

  synchronized long persistPackage(
      final String draftId,
      final JsonNode expectedFinalization,
      final String jarPath,
      final String jarSha256)
      throws IOException {
    JsonNode stored =
        loadOptional(draftId).orElseThrow(() -> new IllegalArgumentException("Draft is missing"));
    if (!(stored instanceof ObjectNode draft)
        || !(draft.get("finalization") instanceof ObjectNode finalization)
        || !sameHostFinalization(expectedFinalization, finalization)
        || !equalText(expectedFinalization, finalization, "deerSha256")
        || !equalText(expectedFinalization, finalization, "finalizedProjectSha256")) {
      throw new FinalizationIdentityConflictException();
    }
    finalization.put("jarPath", jarPath);
    finalization.put("jarSha256", jarSha256);
    draft.put("savedAt", Instant.now().toString());
    long revision = incrementRevision(draft);
    AtomicFiles.replace(draftDirectory(draftId).resolve("draft.json"), AuthoringJson.encode(draft));
    return revision;
  }

  synchronized long requireCurrentRevision(final String draftId, final JsonNode request) {
    JsonNode stored =
        loadOptional(draftId)
            .orElseThrow(() -> new CandidateProjectService.MissingDraftException(draftId));
    long expected = revision(stored);
    long actual = requireRevision(request.get("revision"), "Request revision is invalid");
    if (actual != expected) {
      throw new RevisionConflictException(expected, actual);
    }
    return expected;
  }

  private static String text(final JsonNode node, final String key) {
    JsonNode value = node.get(key);
    return value != null && value.isString() ? value.stringValue() : "";
  }

  private static void mergeFinalization(
      final ObjectNode incomingDraft, final Optional<JsonNode> storedDraft) {
    JsonNode stored = storedDraft.map(draft -> draft.get("finalization")).orElse(null);
    if (!(stored instanceof ObjectNode storedFinalization)) {
      incomingDraft.remove("finalization");
      return;
    }

    ObjectNode merged = storedFinalization.deepCopy();
    JsonNode incoming = incomingDraft.get("finalization");
    if (incoming instanceof ObjectNode incomingFinalization
        && sameHostFinalization(incomingFinalization, storedFinalization)) {
      mergeUiField(merged, incomingFinalization, "candidateHash");
    }
    incomingDraft.set("finalization", merged);
  }

  private static boolean sameHostFinalization(final JsonNode left, final JsonNode right) {
    return left.path("seed").isIntegralNumber()
        && right.path("seed").isIntegralNumber()
        && left.path("seed").longValue() == right.path("seed").longValue()
        && equalText(left, right, "projectDirectory")
        && equalText(left, right, "finalizedAt");
  }

  private static boolean equalText(final JsonNode left, final JsonNode right, final String field) {
    JsonNode leftValue = left.get(field);
    JsonNode rightValue = right.get(field);
    return leftValue != null
        && rightValue != null
        && leftValue.isString()
        && rightValue.isString()
        && leftValue.stringValue().equals(rightValue.stringValue());
  }

  private static void mergeUiField(
      final ObjectNode target, final ObjectNode incoming, final String field) {
    JsonNode value = incoming.get(field);
    if (value != null && value.isString()) {
      target.set(field, value.deepCopy());
    } else {
      target.remove(field);
    }
  }

  private static void mergeHostField(
      final ObjectNode target, final ObjectNode stored, final String field) {
    JsonNode value = stored.get(field);
    if (value != null && value.isString()) {
      target.set(field, value.deepCopy());
    }
  }

  static long revision(final JsonNode draft) {
    return requireRevision(draft.get("revision"), "Stored draft revision is invalid");
  }

  private static long incrementRevision(final ObjectNode draft) {
    long current = revision(draft);
    if (current == MAX_SAFE_INTEGER) {
      throw new IllegalStateException("Draft revision cannot be incremented safely");
    }
    long next = current + 1L;
    draft.put("revision", next);
    return next;
  }

  private static long requireRevision(final JsonNode value, final String message) {
    return requireSafeInteger(value, message);
  }

  static long requireSafeInteger(final JsonNode value, final String message) {
    if (value == null || !value.isIntegralNumber()) {
      throw new IllegalArgumentException(message);
    }
    java.math.BigInteger number = value.bigIntegerValue();
    if (number.signum() < 0
        || number.compareTo(java.math.BigInteger.valueOf(MAX_SAFE_INTEGER)) > 0) {
      throw new IllegalArgumentException(message);
    }
    return number.longValueExact();
  }

  static void requireDraftId(final String draftId) {
    if (draftId == null || !DRAFT_ID.matcher(draftId).matches()) {
      throw new IllegalArgumentException("Draft identifier is invalid");
    }
  }

  static void requireStorageKey(final String storageKey) {
    if (storageKey == null || !STORAGE_KEY.matcher(storageKey).matches()) {
      throw new IllegalArgumentException("Upload storage key is invalid");
    }
  }

  private static byte[] readBounded(final Path path, final int limit) {
    try (InputStream input = Files.newInputStream(path);
        ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[8192];
      int total = 0;
      int read;
      while ((read = input.read(buffer)) >= 0) {
        total += read;
        if (total > limit) {
          throw new IllegalArgumentException("Stored value exceeds its size limit");
        }
        output.write(buffer, 0, read);
      }
      return output.toByteArray();
    } catch (IOException exception) {
      throw new IllegalStateException("Stored value cannot be read", exception);
    }
  }

  private static String sha256(final byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  record Upload(String storageKey, String originalName, String mediaType, byte[] bytes) {
    Upload {
      requireStorageKey(storageKey);
      bytes = bytes.clone();
    }

    @Override
    public byte[] bytes() {
      return bytes.clone();
    }
  }

  static final class RevisionConflictException extends IllegalArgumentException {
    private final long expectedRevision;
    private final long actualRevision;

    RevisionConflictException(final long expectedRevision, final long actualRevision) {
      super("Draft revision conflict");
      this.expectedRevision = expectedRevision;
      this.actualRevision = actualRevision;
    }

    long expectedRevision() {
      return expectedRevision;
    }

    long actualRevision() {
      return actualRevision;
    }
  }

  static final class FinalizationIdentityConflictException extends IllegalArgumentException {
    FinalizationIdentityConflictException() {
      super("Finalization identity does not match the host-confirmed draft finalization");
    }
  }

  static final class ProjectOwnershipConflictException extends IllegalArgumentException {
    ProjectOwnershipConflictException(final Path target, final java.util.Set<String> owners) {
      super(
          owners.isEmpty()
              ? "Project directory is not owned by this draft or already contains unowned files: "
                  + target
              : "Project directory is owned by another draft: "
                  + String.join(", ", owners)
                  + " ("
                  + target
                  + ")");
    }
  }

  static final class ProjectOwnershipUnavailableException extends IllegalStateException {
    ProjectOwnershipUnavailableException(final String message) {
      super(message);
    }

    ProjectOwnershipUnavailableException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }
}

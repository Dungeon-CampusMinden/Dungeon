package wizard.runner.input;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import wizard.runner.contract.ContractCapabilities;
import wizard.runner.contract.IssueCode;
import wizard.runner.contract.IssueCollector;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.contract.ValidationPhase;
import wizard.runner.contract.ValidationSeverity;

/** Strict read-only reader for finalized Wizard project input. */
public final class ProjectInputReader {
  private static final String DEER_FILE = "deer.json";
  private static final ObjectMapper MAPPER = createMapper();

  /** Creates a strict Wizard project reader. */
  public ProjectInputReader() {}

  /**
   * Resolves and reads one project without writing or discovering any neighboring root.
   *
   * @param suppliedProject user-supplied Wizard project directory
   * @return immutable input snapshot and deterministic issues
   */
  public InputSnapshot read(final Path suppliedProject) {
    Path normalized =
        Objects.requireNonNull(suppliedProject, "suppliedProject").toAbsolutePath().normalize();
    IssueCollector issues = new IssueCollector();
    Optional<Path> realRoot = resolveProject(normalized, issues);
    if (realRoot.isEmpty()) {
      return snapshot(realRoot, DeerInput.empty(), issues);
    }

    DeerInput deer = readDeer(realRoot.orElseThrow().resolve(DEER_FILE), issues);
    return snapshot(realRoot, deer, issues);
  }

  private DeerInput readDeer(final Path deerPath, final IssueCollector issues) {
    Optional<BasicFileAttributes> before = resolveDeer(deerPath, issues);
    if (before.isEmpty()) {
      return DeerInput.empty();
    }
    if (before.orElseThrow().size() > ContractCapabilities.MAX_DEER_BYTES) {
      issues.add(
          issue(
              IssueCode.INPUT_DEER_TOO_LARGE,
              "validation.input.deer_too_large",
              Map.of(
                  "actual",
                  before.orElseThrow().size(),
                  "limit",
                  ContractCapabilities.MAX_DEER_BYTES),
              ""));
      return DeerInput.empty();
    }

    Optional<byte[]> rawBytes = readDeerBytes(deerPath, issues);
    if (rawBytes.isEmpty()) {
      return DeerInput.empty();
    }
    byte[] bytes = rawBytes.orElseThrow();
    String rawSha256 = sha256(bytes);
    verifyStable(deerPath, before.orElseThrow(), issues);
    Optional<JsonNode> document = parse(bytes, issues);
    return new DeerInput(Optional.of(rawSha256), document);
  }

  private Optional<Path> resolveProject(final Path project, final IssueCollector issues) {
    try {
      BasicFileAttributes attributes =
          Files.readAttributes(project, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      if (attributes.isSymbolicLink() || attributes.isOther() || !attributes.isDirectory()) {
        issues.add(
            issue(
                IssueCode.INPUT_PROJECT_INVALID,
                "validation.input.project_link_or_not_directory",
                Map.of("reason", "link_or_not_directory"),
                ""));
        return Optional.empty();
      }
      Path realRoot = project.toRealPath();
      if (!Files.isDirectory(realRoot, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(realRoot)) {
        issues.add(
            issue(
                IssueCode.INPUT_PROJECT_INVALID,
                "validation.input.project_unreadable",
                Map.of("reason", "unreadable"),
                ""));
        return Optional.empty();
      }
      return Optional.of(realRoot);
    } catch (IOException | SecurityException exception) {
      issues.add(
          issue(
              IssueCode.INPUT_PROJECT_INVALID,
              "validation.input.project_unreadable",
              Map.of("reason", "missing_or_unreadable"),
              ""));
      return Optional.empty();
    }
  }

  private Optional<BasicFileAttributes> resolveDeer(
      final Path deerPath, final IssueCollector issues) {
    try {
      BasicFileAttributes attributes =
          Files.readAttributes(deerPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      if (attributes.isSymbolicLink()
          || attributes.isOther()
          || !attributes.isRegularFile()
          || !Files.isReadable(deerPath)) {
        issues.add(
            issue(
                IssueCode.INPUT_DEER_UNREADABLE,
                "validation.input.deer_not_regular",
                Map.of("reason", "not_regular_readable_file"),
                ""));
        return Optional.empty();
      }
      return Optional.of(attributes);
    } catch (IOException | SecurityException exception) {
      issues.add(
          issue(
              IssueCode.INPUT_DEER_UNREADABLE,
              "validation.input.deer_unreadable",
              Map.of("reason", "missing_or_unreadable"),
              ""));
      return Optional.empty();
    }
  }

  private Optional<byte[]> readDeerBytes(final Path deerPath, final IssueCollector issues) {
    Set<OpenOption> options = Set.of(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
    try (SeekableByteChannel channel = Files.newByteChannel(deerPath, options)) {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      ByteBuffer buffer = ByteBuffer.allocate(8192);
      int total = 0;
      int read;
      while ((read = channel.read(buffer)) >= 0) {
        if (read == 0) {
          continue;
        }
        total += read;
        if (total > ContractCapabilities.MAX_DEER_BYTES) {
          issues.add(
              issue(
                  IssueCode.INPUT_DEER_TOO_LARGE,
                  "validation.input.deer_too_large",
                  Map.of("actual", total, "limit", ContractCapabilities.MAX_DEER_BYTES),
                  ""));
          return Optional.empty();
        }
        output.write(buffer.array(), 0, read);
        buffer.clear();
      }
      return Optional.of(output.toByteArray());
    } catch (IOException | SecurityException exception) {
      issues.add(
          issue(
              IssueCode.INPUT_DEER_UNREADABLE,
              "validation.input.deer_unreadable",
              Map.of("reason", "read_failed"),
              ""));
      return Optional.empty();
    }
  }

  private void verifyStable(
      final Path deerPath, final BasicFileAttributes before, final IssueCollector issues) {
    try {
      BasicFileAttributes after =
          Files.readAttributes(deerPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
      if (after.isSymbolicLink()
          || after.isOther()
          || !after.isRegularFile()
          || before.size() != after.size()
          || !before.lastModifiedTime().equals(after.lastModifiedTime())
          || !Objects.equals(before.fileKey(), after.fileKey())) {
        issues.add(
            issue(
                IssueCode.INPUT_CHANGED_DURING_RUN,
                "validation.input.changed_during_read",
                Map.of("reason", "attributes_changed"),
                ""));
      }
    } catch (IOException | SecurityException exception) {
      issues.add(
          issue(
              IssueCode.INPUT_CHANGED_DURING_RUN,
              "validation.input.changed_during_read",
              Map.of("reason", "attributes_unavailable"),
              ""));
    }
  }

  private Optional<JsonNode> parse(final byte[] bytes, final IssueCollector issues) {
    if (hasBom(bytes)) {
      issues.add(issue(IssueCode.INPUT_UTF8_BOM, "validation.input.utf8_bom", Map.of(), ""));
      return Optional.empty();
    }
    String json;
    try {
      json =
          StandardCharsets.UTF_8
              .newDecoder()
              .onMalformedInput(CodingErrorAction.REPORT)
              .onUnmappableCharacter(CodingErrorAction.REPORT)
              .decode(ByteBuffer.wrap(bytes))
              .toString();
    } catch (CharacterCodingException exception) {
      issues.add(
          issue(IssueCode.INPUT_UTF8_INVALID, "validation.input.utf8_invalid", Map.of(), ""));
      return Optional.empty();
    }

    JsonNode root;
    try {
      root = MAPPER.readTree(json);
    } catch (StreamReadException exception) {
      boolean duplicate = exception.getOriginalMessage().startsWith("Duplicate Object property ");
      issues.add(
          issue(
              duplicate ? IssueCode.JSON_DUPLICATE_KEY : IssueCode.JSON_PARSE_INVALID,
              duplicate
                  ? "validation.input.json_duplicate_key"
                  : "validation.input.json_parse_invalid",
              Map.of(),
              ""));
      return Optional.empty();
    } catch (JacksonException exception) {
      issues.add(
          issue(IssueCode.JSON_PARSE_INVALID, "validation.input.json_parse_invalid", Map.of(), ""));
      return Optional.empty();
    }
    if (root == null) {
      issues.add(
          issue(IssueCode.JSON_PARSE_INVALID, "validation.input.json_parse_invalid", Map.of(), ""));
      return Optional.empty();
    }
    if (!root.isObject()) {
      issues.add(
          new ValidationIssue(
              ValidationSeverity.ERROR,
              ValidationPhase.SCHEMA,
              IssueCode.SCHEMA_INVALID,
              "validation.schema.root_object",
              Map.of("actual", root.getNodeType().name().toLowerCase(), "expected", "object"),
              "",
              Optional.empty(),
              List.of()));
      return Optional.empty();
    }
    validateUnicode(root, "", issues);
    validateFormat(root, issues);
    return Optional.of(root);
  }

  private void validateFormat(final JsonNode root, final IssueCollector issues) {
    JsonNode format = root.get("formatVersion");
    Set<String> supported = ContractCapabilities.DEER_FORMAT_VERSIONS;
    if (format == null || !format.isString() || !supported.contains(format.stringValue())) {
      String actual =
          format == null
              ? "missing"
              : format.isString()
                  ? hasUnpairedSurrogate(format.stringValue())
                      ? "invalid_unicode"
                      : format.stringValue()
                  : format.getNodeType().name().toLowerCase();
      issues.add(
          issue(
              IssueCode.FORMAT_VERSION_UNSUPPORTED,
              "validation.input.format_version_unsupported",
              Map.of(
                  "actual",
                  actual,
                  "expected",
                  String.join(",", supported.stream().sorted().toList())),
              "/formatVersion"));
    }
  }

  private void validateUnicode(
      final JsonNode node, final String pointer, final IssueCollector issues) {
    if (node.isString()) {
      if (hasUnpairedSurrogate(node.stringValue())) {
        issues.add(
            issue(
                IssueCode.JSON_UNICODE_INVALID,
                "validation.input.json_unicode_invalid",
                Map.of(),
                pointer));
      }
      return;
    }
    if (node.isArray()) {
      for (int index = 0; index < node.size(); index++) {
        validateUnicode(node.get(index), pointer + "/" + index, issues);
      }
      return;
    }
    if (node.isObject()) {
      int memberIndex = 0;
      for (Map.Entry<String, JsonNode> entry : node.properties()) {
        if (hasUnpairedSurrogate(entry.getKey())) {
          issues.add(
              issue(
                  IssueCode.JSON_UNICODE_INVALID,
                  "validation.input.json_unicode_invalid",
                  Map.of("location", "member_name", "memberIndex", memberIndex),
                  pointer));
          validateUnicode(entry.getValue(), pointer, issues);
        } else {
          validateUnicode(entry.getValue(), pointer + "/" + escapePointer(entry.getKey()), issues);
        }
        memberIndex++;
      }
    }
  }

  private static boolean hasUnpairedSurrogate(final String value) {
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      if (Character.isHighSurrogate(character)) {
        if (index + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(index + 1))) {
          return true;
        }
        index++;
      } else if (Character.isLowSurrogate(character)) {
        return true;
      }
    }
    return false;
  }

  private static String escapePointer(final String value) {
    return value.replace("~", "~0").replace("/", "~1");
  }

  private static boolean hasBom(final byte[] bytes) {
    return bytes.length >= 3
        && Byte.toUnsignedInt(bytes[0]) == 0xEF
        && Byte.toUnsignedInt(bytes[1]) == 0xBB
        && Byte.toUnsignedInt(bytes[2]) == 0xBF;
  }

  private static String sha256(final byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  private static ObjectMapper createMapper() {
    JsonFactory factory =
        JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();
    return JsonMapper.builder(factory)
        .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
        .build();
  }

  private static ValidationIssue issue(
      final IssueCode code,
      final String messageKey,
      final Map<String, Object> arguments,
      final String path) {
    return new ValidationIssue(
        ValidationSeverity.ERROR,
        ValidationPhase.INPUT,
        code,
        messageKey,
        arguments,
        path,
        Optional.empty(),
        List.of());
  }

  private static InputSnapshot snapshot(
      final Optional<Path> realRoot, final DeerInput deer, final IssueCollector issues) {
    return new InputSnapshot(realRoot, deer.rawSha256(), deer.document(), issues.issues());
  }

  private record DeerInput(Optional<String> rawSha256, Optional<JsonNode> document) {
    private static DeerInput empty() {
      return new DeerInput(Optional.empty(), Optional.empty());
    }
  }
}

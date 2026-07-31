package wizard.runner.input;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import wizard.runner.contract.ValidationIssue;

/** Immutable result of resolving and strictly reading one Wizard project input. */
public final class InputSnapshot {
  private final Optional<Path> realProjectRoot;
  private final Optional<String> rawDeerSha256;
  private final Optional<JsonNode> document;
  private final List<ValidationIssue> issues;

  InputSnapshot(
      final Optional<Path> realProjectRoot,
      final Optional<String> rawDeerSha256,
      final Optional<JsonNode> document,
      final List<ValidationIssue> issues) {
    this.realProjectRoot = Objects.requireNonNull(realProjectRoot, "realProjectRoot");
    this.rawDeerSha256 = Objects.requireNonNull(rawDeerSha256, "rawDeerSha256");
    this.document = copyDocument(document);
    this.issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
  }

  /**
   * Returns the resolved real project root when the project boundary was valid.
   *
   * @return optional real project root
   */
  public Optional<Path> realProjectRoot() {
    return realProjectRoot;
  }

  /**
   * Returns lowercase SHA-256 of the exact readable DEER bytes.
   *
   * @return optional raw-byte digest
   */
  public Optional<String> rawDeerSha256() {
    return rawDeerSha256;
  }

  /**
   * Returns a defensive deep copy of the parsed root object when JSON parsing succeeded.
   *
   * @return optional copied JSON document
   */
  public Optional<JsonNode> document() {
    return copyDocument(document);
  }

  /**
   * Returns deterministic immutable input issues.
   *
   * @return sorted issue list
   */
  public List<ValidationIssue> issues() {
    return issues;
  }

  private static Optional<JsonNode> copyDocument(final Optional<JsonNode> document) {
    Objects.requireNonNull(document, "document");
    return document.map(JsonNode::deepCopy);
  }
}

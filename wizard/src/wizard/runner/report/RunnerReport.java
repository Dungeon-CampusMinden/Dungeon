package wizard.runner.report;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import wizard.runner.RunnerRequest.Command;
import wizard.runner.RunnerVersion;
import wizard.runner.canonical.CanonicalJson;
import wizard.runner.contract.IssueCollector;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.contract.ValidationSeverity;
import wizard.runner.validation.ValidationResult;

/** Immutable canonical machine report emitted by Wizard runner commands. */
public final class RunnerReport {
  private final Command command;
  private final boolean valid;
  private final Optional<RunnerVersion> version;
  private final Optional<String> rawDeerSha256;
  private final Optional<String> hostInputSha256;
  private final List<ValidationIssue> issues;

  private RunnerReport(
      final Command command,
      final Optional<RunnerVersion> version,
      final boolean valid,
      final Optional<String> rawDeerSha256,
      final Optional<String> hostInputSha256,
      final List<ValidationIssue> issues) {
    this.command = Objects.requireNonNull(command, "command");
    this.version = Objects.requireNonNull(version, "version");
    this.rawDeerSha256 = Objects.requireNonNull(rawDeerSha256, "rawDeerSha256");
    this.hostInputSha256 = Objects.requireNonNull(hostInputSha256, "hostInputSha256");
    this.issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    boolean containsBlockingIssue =
        this.issues.stream().anyMatch(issue -> issue.severity() == ValidationSeverity.ERROR);
    if (valid == containsBlockingIssue) {
      throw new IllegalArgumentException("valid must be the inverse of blocking issue presence");
    }
    this.valid = valid;
  }

  /**
   * Creates a report from the single production validation result.
   *
   * @param command command represented by the report
   * @param version loaded runner and report version authority
   * @param result immutable production validation result
   * @return deterministic validation report
   */
  public static RunnerReport validation(
      final Command command, final RunnerVersion version, final ValidationResult result) {
    Objects.requireNonNull(result, "result");
    return new RunnerReport(
        command,
        Optional.of(Objects.requireNonNull(version, "version")),
        result.valid(),
        result.rawDeerSha256(),
        result.hostInputSha256(),
        result.issues());
  }

  /**
   * Returns a failed report retaining all existing diagnostics plus one stable failure.
   *
   * @param issue additional blocking failure
   * @return deterministic failed report with normatively sorted diagnostics
   */
  public RunnerReport withFailure(final ValidationIssue issue) {
    IssueCollector collector = new IssueCollector();
    collector.addAll(issues);
    collector.add(Objects.requireNonNull(issue, "issue"));
    return new RunnerReport(
        command, version, false, rawDeerSha256, hostInputSha256, collector.issues());
  }

  /**
   * Returns the canonical UTF-8 report content as text without surrounding whitespace.
   *
   * @return deterministic canonical JSON report text
   */
  public String canonicalJson() {
    Map<String, Object> envelope = new LinkedHashMap<>();
    envelope.put("command", command.name().toLowerCase(Locale.ROOT));
    envelope.put("valid", valid);
    envelope.put("runnerVersion", version.map(RunnerVersion::runnerVersion).orElse(null));
    envelope.put("rawDeerSha256", rawDeerSha256.orElse(null));
    envelope.put("hostInputSha256", hostInputSha256.orElse(null));
    envelope.put("issues", issues.stream().map(RunnerReport::issueObject).toList());
    return CanonicalJson.encode(envelope);
  }

  private static Map<String, Object> issueObject(final ValidationIssue issue) {
    Map<String, Object> value = new LinkedHashMap<>();
    value.put("severity", issue.severity().name().toLowerCase(Locale.ROOT));
    value.put("phase", issue.phase().name().toLowerCase(Locale.ROOT));
    value.put("code", issue.code().name());
    value.put("messageKey", issue.messageKey());
    value.put("arguments", issue.arguments());
    value.put("path", issue.path());
    value.put(
        "entity",
        issue
            .entity()
            .map(
                entity -> {
                  Map<String, Object> identity = new LinkedHashMap<>();
                  identity.put("kind", entity.kind());
                  identity.put("id", entity.id());
                  return identity;
                })
            .orElse(null));
    value.put("relatedPaths", new ArrayList<>(issue.relatedPaths()));
    return value;
  }
}

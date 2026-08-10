package wizard.runner.validation;

import foundation.room.model.VerifiedAsset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.contract.ValidationSeverity;
import wizard.runner.model.ProjectDefinition;

/**
 * Immutable result of the strict production validation pipeline.
 *
 * @param valid whether validated room inputs are available
 * @param rawDeerSha256 exact DEER byte hash when readable
 * @param hostInputSha256 canonical host-input hash when valid
 * @param model immutable mapped model when valid
 * @param assets immutable verified referenced assets when valid
 * @param issues deterministic diagnostics
 */
public record ValidationResult(
    boolean valid,
    Optional<String> rawDeerSha256,
    Optional<String> hostInputSha256,
    Optional<ProjectDefinition> model,
    List<VerifiedAsset> assets,
    List<ValidationIssue> issues) {

  /** Creates a closed validation result whose runtime inputs exist only when valid. */
  public ValidationResult {
    rawDeerSha256 = Objects.requireNonNull(rawDeerSha256, "rawDeerSha256");
    hostInputSha256 = Objects.requireNonNull(hostInputSha256, "hostInputSha256");
    model = Objects.requireNonNull(model, "model");
    assets = List.copyOf(Objects.requireNonNull(assets, "assets"));
    issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    if (valid != model.isPresent()) {
      throw new IllegalArgumentException("model must be present exactly when valid");
    }
    if (valid != hostInputSha256.isPresent()) {
      throw new IllegalArgumentException("host input hash must be present exactly when valid");
    }
    if (!valid && !assets.isEmpty()) {
      throw new IllegalArgumentException("invalid results must not expose verified assets");
    }
    boolean hasErrors =
        issues.stream().anyMatch(issue -> issue.severity() == ValidationSeverity.ERROR);
    if (valid && hasErrors) {
      throw new IllegalArgumentException("valid results must not contain error issues");
    }
    if (!valid && !hasErrors) {
      throw new IllegalArgumentException("invalid results must contain at least one error issue");
    }
  }
}

package wizard.runner;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import wizard.runner.contract.IssueCode;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.contract.ValidationPhase;
import wizard.runner.contract.ValidationSeverity;
import wizard.runner.report.ProjectValidationReport;
import wizard.runner.room.RoomDeriver;
import wizard.runner.validation.ProjectValidationPipeline;
import wizard.runner.validation.ValidationResult;

/** Shared production validation and room-feasibility boundary for all Wizard entry points. */
public final class ProjectValidationService {
  /**
   * Validates an exact finalized project and verifies that its room can be derived.
   *
   * @param project finalized project directory
   * @return validation report and successful closed input
   */
  public Outcome validate(final Path project) {
    ValidationResult validation =
        new ProjectValidationPipeline()
            .validate(Objects.requireNonNull(project, "project").toAbsolutePath().normalize());
    ProjectValidationReport report = ProjectValidationReport.from(RunnerVersion.load(), validation);
    if (validation.valid()) {
      try {
        new RoomDeriver().derive(validation);
      } catch (RuntimeException exception) {
        report = report.withFailure(derivationFailure());
      }
    }
    return new Outcome(report, report.valid() ? Optional.of(validation) : Optional.empty());
  }

  private static ValidationIssue derivationFailure() {
    return new ValidationIssue(
        ValidationSeverity.ERROR,
        ValidationPhase.FEASIBILITY,
        IssueCode.INTERNAL_ERROR,
        "validation.derivation.failed",
        Map.of(),
        "",
        Optional.empty(),
        List.of());
  }

  /**
   * Validation report plus the closed validated input, available only for a successful report.
   *
   * @param report stable production validation report
   * @param validation closed input when the report is valid
   */
  public record Outcome(ProjectValidationReport report, Optional<ValidationResult> validation) {
    /**
     * Enforces agreement between report validity and the optional closed validation input.
     *
     * @param report stable production validation report
     * @param validation closed input when the report is valid
     */
    public Outcome {
      Objects.requireNonNull(report, "report");
      Objects.requireNonNull(validation, "validation");
      if (report.valid() != validation.isPresent()) {
        throw new IllegalArgumentException("validation must be present exactly for a valid report");
      }
    }
  }
}

package wizard.runner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import wizard.runner.contract.IssueCode;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.contract.ValidationPhase;
import wizard.runner.contract.ValidationSeverity;
import wizard.runner.report.ProjectValidationReport;
import wizard.runner.room.RoomDeriver;
import wizard.runner.validation.ProjectValidationPipeline;
import wizard.runner.validation.ValidationResult;

/** Internal Gradle entry point that validates a project before packaging. */
final class PackagingProjectValidator {
  private static final int SUCCESS = 0;
  private static final int FAILURE = 1;

  private PackagingProjectValidator() {}

  public static void main(final String[] arguments) {
    if (arguments.length != 1) {
      throw new IllegalArgumentException("Expected exactly one Wizard project path");
    }

    ValidationResult validation =
        new ProjectValidationPipeline()
            .validate(Path.of(arguments[0]).toAbsolutePath().normalize());
    ProjectValidationReport report = ProjectValidationReport.from(RunnerVersion.load(), validation);
    int exitCode = FAILURE;
    if (validation.valid()) {
      try {
        new RoomDeriver().derive(validation);
        exitCode = SUCCESS;
      } catch (RuntimeException exception) {
        report =
            report.withFailure(
                new ValidationIssue(
                    ValidationSeverity.ERROR,
                    ValidationPhase.FEASIBILITY,
                    IssueCode.INTERNAL_ERROR,
                    "validation.derivation.failed",
                    Map.of(),
                    "",
                    Optional.empty(),
                    List.of()));
      }
    }

    byte[] reportBytes = report.canonicalJson().getBytes(StandardCharsets.UTF_8);
    System.out.write(reportBytes, 0, reportBytes.length);
    System.out.flush();
    if (exitCode != SUCCESS) {
      System.exit(exitCode);
    }
  }
}

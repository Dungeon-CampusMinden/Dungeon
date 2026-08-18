package wizard.runner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import wizard.runner.report.ProjectValidationReport;

/** Internal Gradle entry point that validates a project before packaging. */
final class PackagingProjectValidator {
  private static final int SUCCESS = 0;
  private static final int FAILURE = 1;

  private PackagingProjectValidator() {}

  public static void main(final String[] arguments) {
    if (arguments.length != 1) {
      throw new IllegalArgumentException("Expected exactly one Wizard project path");
    }

    ProjectValidationReport report =
        new ProjectValidationService()
            .validate(Path.of(arguments[0]).toAbsolutePath().normalize())
            .report();
    int exitCode = report.valid() ? SUCCESS : FAILURE;

    byte[] reportBytes = report.canonicalJson().getBytes(StandardCharsets.UTF_8);
    System.out.write(reportBytes, 0, reportBytes.length);
    System.out.flush();
    if (exitCode != SUCCESS) {
      System.exit(exitCode);
    }
  }
}

package wizard.runner;

import core.utils.logging.DungeonLoggerConfig;
import foundation.room.model.FoundationRoom;
import java.io.PrintStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import wizard.runner.RunnerRequest.Command;
import wizard.runner.contract.IssueCode;
import wizard.runner.contract.ValidationIssue;
import wizard.runner.contract.ValidationPhase;
import wizard.runner.contract.ValidationSeverity;
import wizard.runner.report.RunnerReport;
import wizard.runner.room.RoomDeriver;
import wizard.runner.runtime.multiplayer.MultiplayerHostRun;
import wizard.runner.runtime.multiplayer.MultiplayerJoinRun;
import wizard.runner.validation.ProjectValidationPipeline;
import wizard.runner.validation.ValidationResult;

/** Strict, case-sensitive command-line entry point for the generic Wizard Runner. */
public final class RunnerCli {
  private static final int SUCCESS = 0;
  private static final int FAILURE = 1;
  private static final String USAGE =
      "Usage:\n"
          + "  wizard-runner validate --project <folder>\n"
          + "  wizard-runner host     --project <folder>\n"
          + "  wizard-runner join     --project <folder>\n"
          + "  wizard-runner --help\n"
          + "  wizard-runner --version\n";

  private RunnerCli() {}

  /**
   * Runs the CLI and terminates the process when the command fails.
   *
   * @param arguments case-sensitive command-line tokens
   */
  public static void main(final String[] arguments) {
    int exitCode = run(arguments, System.out, System.err);
    if (exitCode != SUCCESS) {
      System.exit(exitCode);
    }
  }

  /**
   * Runs the CLI against caller-provided streams.
   *
   * @param arguments case-sensitive command-line tokens
   * @param standardOutput standard-output stream
   * @param standardError standard-error stream
   * @return {@code 0} on success, otherwise {@code 1}
   */
  public static int run(
      final String[] arguments, final PrintStream standardOutput, final PrintStream standardError) {
    return run(arguments, standardOutput, standardError, RunnerCli::execute);
  }

  static int run(
      final String[] arguments,
      final PrintStream standardOutput,
      final PrintStream standardError,
      final CommandExecutor executor) {
    Objects.requireNonNull(arguments, "arguments");
    Objects.requireNonNull(executor, "executor");
    RunnerResult result;
    try {
      result = dispatch(arguments.clone(), executor);
    } catch (UsageException exception) {
      result = new RunnerResult(FAILURE, "", exception.getMessage() + "\n" + USAGE);
    } catch (IllegalStateException exception) {
      result =
          new RunnerResult(
              FAILURE, "", "Runner resources are invalid: " + exception.getMessage() + "\n");
    }
    return result.writeTo(standardOutput, standardError);
  }

  private static RunnerResult dispatch(final String[] arguments, final CommandExecutor executor)
      throws UsageException {
    if (arguments.length == 0) {
      throw usage("Missing command.");
    }
    if ("--help".equals(arguments[0])) {
      requireSingleToken(arguments, "--help");
      return new RunnerResult(SUCCESS, USAGE, "");
    }
    if ("--version".equals(arguments[0])) {
      requireSingleToken(arguments, "--version");
      RunnerVersion version = RunnerVersion.load();
      return new RunnerResult(SUCCESS, "wizard-runner " + version.runnerVersion() + "\n", "");
    }

    RunnerRequest request = parseRequest(parseCommand(arguments[0]), arguments);
    return executor.execute(request, RunnerVersion.load());
  }

  private static RunnerRequest parseRequest(final Command command, final String[] arguments)
      throws UsageException {
    String project = null;
    for (int index = 1; index < arguments.length; index++) {
      String option = arguments[index];
      switch (option) {
        case "--project" -> {
          if (project != null) {
            throw usage("Repeated option: --project");
          }
          project = requireValue(arguments, ++index, "--project");
        }
        case "--assets" -> throw usage("Unknown option for " + command.token() + ": --assets");
        default -> throw usage("Unknown option or positional argument: " + option);
      }
    }

    if (project == null) {
      throw usage("Missing required option: --project");
    }
    return new RunnerRequest(command, normalizePath(project, "--project"));
  }

  private static RunnerResult execute(final RunnerRequest request, final RunnerVersion version) {
    return switch (request.command()) {
      case VALIDATE -> validate(request, version);
      case HOST -> host(request, version);
      case JOIN -> join(request, version);
    };
  }

  private static RunnerResult validate(final RunnerRequest request, final RunnerVersion version) {
    ValidationResult validation = new ProjectValidationPipeline().validate(request.project());
    RunnerReport report = RunnerReport.validation(request.command(), version, validation);
    if (!validation.valid()) {
      return result(FAILURE, report);
    }
    try {
      new RoomDeriver().derive(validation);
      return result(SUCCESS, report);
    } catch (RuntimeException exception) {
      return startFailure(report);
    }
  }

  private static RunnerResult host(final RunnerRequest request, final RunnerVersion version) {
    ValidationResult validation = new ProjectValidationPipeline().validate(request.project());
    RunnerReport report = RunnerReport.validation(request.command(), version, validation);
    if (!validation.valid()) {
      return result(FAILURE, report);
    }
    try {
      FoundationRoom room = new RoomDeriver().derive(validation);
      DisposableRunnerRuntime.run(
          runtime -> {
            MultiplayerHostRun.from(room).run();
            return null;
          });
      return result(SUCCESS, report);
    } catch (RuntimeException exception) {
      return startFailure(report);
    }
  }

  private static RunnerResult join(final RunnerRequest request, final RunnerVersion version) {
    ValidationResult validation = new ProjectValidationPipeline().validate(request.project());
    RunnerReport report = RunnerReport.validation(request.command(), version, validation);
    if (!validation.valid()) {
      return result(FAILURE, report);
    }
    try {
      FoundationRoom room = new RoomDeriver().derive(validation);
      DisposableRunnerRuntime.run(
          runtime -> {
            try {
              MultiplayerJoinRun.from(room).run();
            } finally {
              DungeonLoggerConfig.shutdown();
            }
            return null;
          });
      return result(SUCCESS, report);
    } catch (RuntimeException exception) {
      return result(
          FAILURE,
          report.withFailure(
              issue(
                  ValidationPhase.FEASIBILITY,
                  IssueCode.CLIENT_BOOTSTRAP_FAILED,
                  "runner.bootstrap.failed")));
    }
  }

  private static RunnerResult startFailure(final RunnerReport report) {
    return result(
        FAILURE,
        report.withFailure(
            issue(
                ValidationPhase.FEASIBILITY,
                IssueCode.RUNNER_START_FAILED,
                "runner.start.failed")));
  }

  private static RunnerResult result(final int exitCode, final RunnerReport report) {
    return new RunnerResult(exitCode, report.canonicalJson(), "");
  }

  private static ValidationIssue issue(
      final ValidationPhase phase, final IssueCode code, final String messageKey) {
    return new ValidationIssue(
        ValidationSeverity.ERROR,
        phase,
        code,
        messageKey,
        Map.of(),
        "",
        Optional.empty(),
        List.of());
  }

  private static Command parseCommand(final String token) throws UsageException {
    return switch (token) {
      case "validate" -> Command.VALIDATE;
      case "host" -> Command.HOST;
      case "join" -> Command.JOIN;
      default -> throw usage("Unknown command: " + token);
    };
  }

  private static void requireSingleToken(final String[] arguments, final String command)
      throws UsageException {
    if (arguments.length != 1) {
      throw usage(command + " does not accept arguments.");
    }
  }

  private static String requireValue(final String[] arguments, final int index, final String option)
      throws UsageException {
    if (index >= arguments.length || arguments[index].startsWith("--")) {
      throw usage("Missing value for option: " + option);
    }
    if (arguments[index].isEmpty()) {
      throw usage("Empty value for option: " + option);
    }
    return arguments[index];
  }

  private static Path normalizePath(final String value, final String option) throws UsageException {
    try {
      return Path.of(value).toAbsolutePath().normalize();
    } catch (InvalidPathException | SecurityException exception) {
      throw usage("Invalid path for option: " + option);
    }
  }

  private static UsageException usage(final String message) {
    return new UsageException(message);
  }

  @FunctionalInterface
  interface CommandExecutor {
    RunnerResult execute(RunnerRequest request, RunnerVersion version);
  }

  private static final class UsageException extends Exception {
    private UsageException(final String message) {
      super(message);
    }
  }
}

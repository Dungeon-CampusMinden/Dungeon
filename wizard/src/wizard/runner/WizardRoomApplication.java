package wizard.runner;

import engine.game.GameStarter;
import engine.game.ServerProcess;
import engine.language.Language;
import engine.utils.logging.DungeonLoggerConfig;
import escaperoom.foundation.room.model.FoundationRoom;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import wizard.runner.bootstrap.JoinBootstrapException;
import wizard.runner.room.RoomDeriver;
import wizard.runner.runtime.multiplayer.MultiplayerHostRun;
import wizard.runner.runtime.multiplayer.MultiplayerJoinRun;
import wizard.runner.validation.ProjectValidationPipeline;
import wizard.runner.validation.ValidationResult;

/** Player-facing entry point for one packaged Wizard room. */
public final class WizardRoomApplication {
  private static final int SUCCESS = 0;
  private static final int FAILURE = 1;
  private static final String USAGE = "Usage: java -jar WizardRoom.jar [--server]";

  private WizardRoomApplication() {}

  /**
   * Opens the room's client main menu or runs its managed authoritative server.
   *
   * <p>No arguments start the client menu. Exactly {@code --server} starts the headless host used
   * by that menu.
   *
   * @param arguments no arguments for the menu, or exactly {@code --server}
   */
  public static void main(final String[] arguments) {
    int exitCode = run(arguments, System.err);
    if (exitCode != SUCCESS) {
      System.exit(exitCode);
    }
  }

  static int run(final String[] arguments, final PrintStream standardError) {
    Objects.requireNonNull(arguments, "arguments");
    Objects.requireNonNull(standardError, "standardError");
    boolean server = arguments.length == 1 && ServerProcess.SERVER_ARGUMENT.equals(arguments[0]);
    if (arguments.length > 1 || (arguments.length == 1 && !server)) {
      standardError.println(
          "Unsupported Wizard room arguments: " + Arrays.toString(arguments) + "\n" + USAGE);
      return FAILURE;
    }

    try {
      return DisposableRunnerRuntime.run(
          runtime -> {
            try {
              Path project = EmbeddedProjectMaterializer.materialize(runtime);
              ValidationResult validation = new ProjectValidationPipeline().validate(project);
              if (!validation.valid()) {
                standardError.println("Embedded Wizard room validation failed.");
                return FAILURE;
              }
              if (server) {
                FoundationRoom room = new RoomDeriver().derive(validation);
                MultiplayerHostRun.from(room).run();
                return SUCCESS;
              }
              return runClient(validation, standardError);
            } finally {
              DungeonLoggerConfig.shutdown();
            }
          });
    } catch (RuntimeException exception) {
      standardError.println(
          "Wizard room could not start: " + conciseMessage(exception, "unknown runtime failure"));
      return FAILURE;
    }
  }

  private static int runClient(final ValidationResult validation, final PrintStream standardError) {
    String title = validation.model().orElseThrow().metadata().title();
    GameStarter game =
        GameStarter.builder(title, WizardRoomApplication.class).language(Language.DE).build();
    try {
      MultiplayerJoinRun.from(new RoomDeriver().derive(validation)).runMainMenu(game);
      return SUCCESS;
    } catch (JoinBootstrapException exception) {
      standardError.println(
          "Wizard room client bootstrap failed: "
              + conciseMessage(exception, "unknown bootstrap failure"));
      return FAILURE;
    }
  }

  private static String conciseMessage(final RuntimeException exception, final String fallback) {
    String message = exception.getMessage();
    return message == null || message.isBlank() ? fallback : message;
  }
}

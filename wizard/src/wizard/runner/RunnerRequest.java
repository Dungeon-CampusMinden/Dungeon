package wizard.runner;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Fully tokenized Runner command with one normalized filesystem input.
 *
 * @param command Runner operation
 * @param project Wizard project root for every room operation
 */
public record RunnerRequest(Command command, Path project) {
  /** Creates a request after strict command-line token validation. */
  public RunnerRequest {
    Objects.requireNonNull(command, "command");
    project = Objects.requireNonNull(project, "project").toAbsolutePath().normalize();
  }

  /** Stable strict Runner operations. */
  public enum Command {
    /** Validate and derive a Wizard project without starting a process. */
    VALIDATE("validate"),
    /** Start a flexible authoritative multiplayer host. */
    HOST("host"),
    /** Join a host using the complete local Wizard project. */
    JOIN("join");

    private final String token;

    Command(final String token) {
      this.token = token;
    }

    /**
     * Returns the case-sensitive CLI token.
     *
     * @return command token
     */
    public String token() {
      return token;
    }
  }
}

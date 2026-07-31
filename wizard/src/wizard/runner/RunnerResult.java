package wizard.runner;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Immutable CLI result containing the exact stdout, stderr, and process status.
 *
 * @param exitCode process exit code
 * @param stdout exact standard-output content
 * @param stderr exact standard-error content
 */
public record RunnerResult(int exitCode, String stdout, String stderr) {
  /**
   * Creates an immutable command result.
   *
   * @param exitCode process exit code
   * @param stdout exact standard-output content
   * @param stderr exact standard-error content
   */
  public RunnerResult {
    Objects.requireNonNull(stdout, "stdout");
    Objects.requireNonNull(stderr, "stderr");
  }

  /**
   * Writes this result to the provided streams without closing them.
   *
   * @param standardOutput standard-output stream
   * @param standardError standard-error stream
   * @return numeric process exit code
   */
  public int writeTo(final PrintStream standardOutput, final PrintStream standardError) {
    writeUtf8(Objects.requireNonNull(standardOutput, "standardOutput"), stdout);
    writeUtf8(Objects.requireNonNull(standardError, "standardError"), stderr);
    return exitCode;
  }

  private static void writeUtf8(final PrintStream stream, final String content) {
    byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
    stream.write(bytes, 0, bytes.length);
    stream.flush();
  }
}

package dgir.vm.api;

import core.ir.SourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a source-level breakpoint registered with the {@link Debugger}.
 *
 * <p>A breakpoint matches an operation if the operation's {@link SourceLocation} refers to the same
 * file and the same line number. Column information is ignored during matching so that a single
 * line breakpoint hits all operations emitted for that line.
 */
public record Breakpoint(@NotNull String file, int line) {

  /**
   * Construct a breakpoint from an existing {@link SourceLocation}.
   *
   * @param location the source location to break at.
   */
  public static @NotNull Breakpoint of(@NotNull SourceLocation location) {
    return new Breakpoint(location.file(), location.line());
  }

  /**
   * Returns {@code true} if the given location is on this breakpoint's file and line.
   *
   * @param location the location to test.
   * @return whether the location matches this breakpoint.
   */
  public boolean matches(@NotNull SourceLocation location) {
    return line == location.line() && file.equals(location.file());
  }

  public @NotNull String toString() {
    return file + ":" + line;
  }
}

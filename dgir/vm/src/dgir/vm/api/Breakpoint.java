package dgir.vm.api;

import core.ir.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a source-level breakpoint registered with the {@link Debugger}.
 *
 * <p>A breakpoint matches an operation if the operation's {@link Location} refers to the same file
 * and line. When {@code column} is {@code 0} the column is treated as a wildcard and any column on
 * the given line will match. When {@code column} is non-zero, both line and column must match
 * exactly, allowing column-precise breakpoints.
 */
public record Breakpoint(@NotNull String file, int line, int column) {

  /**
   * Construct a breakpoint from an existing {@link Location}.
   *
   * @param location the source location to break at.
   */
  public static @NotNull Breakpoint of(@NotNull Location location) {
    return new Breakpoint(location.file(), location.line(), location.column());
  }

  /**
   * Returns {@code true} if the given location is on this breakpoint's file and line.
   * When this breakpoint's {@code column} is {@code 0} any column matches (line-level
   * breakpoint); otherwise the column must also match exactly.
   *
   * @param location the location to test.
   * @return whether the location matches this breakpoint.
   */
  public boolean matches(@NotNull Location location) {
    if (!file.equals(location.file()) || line != location.line()) return false;
    return column == 0 || column == location.column();
  }

  public @NotNull String toString() {
    return file + ":" + line + (column != 0 ? ":" + column : "");
  }
}
